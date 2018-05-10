package org.thoughtcrime.securesms.contactshare.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.attachments.PointerAttachment;
import org.thoughtcrime.securesms.events.PartProgressEvent;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.shared.SharedContact;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContactModelMapper {

  private static final String TAG = ContactModelMapper.class.getSimpleName();

  public static List<SharedContact> localToRemoteWithAttachments(@NonNull Context context, @NonNull List<Contact> contacts) {
    return localToRemote(context, contacts);
  }

  public static List<SharedContact> localToRemote(@NonNull List<Contact> contacts) {
    return localToRemote(null, contacts);
  }

  private static List<SharedContact> localToRemote(@Nullable Context context, @NonNull List<Contact> contacts) {
    List<SharedContact> sharedContacts = new ArrayList<>(contacts.size());

    for (Contact contact : contacts) {
      sharedContacts.add(localToRemote(context, contact));
    }

    return sharedContacts;
  }

  private static SharedContact localToRemote(@NonNull Contact contact) {
    return localToRemote(null, contact);
  }

  private static SharedContact localToRemote(@Nullable Context context, @NonNull Contact contact) {
    List<SharedContact.Phone>         phoneNumbers    = new ArrayList<>(contact.getPhoneNumbers().size());
    List<SharedContact.Email>         emails          = new ArrayList<>(contact.getEmails().size());
    List<SharedContact.PostalAddress> postalAddresses = new ArrayList<>(contact.getPostalAddresses().size());

    for (Phone phone : contact.getPhoneNumbers()) {
      phoneNumbers.add(new SharedContact.Phone.Builder().setValue(phone.getNumber())
          .setType(localToRemoteType(phone.getType()))
          .setLabel(phone.getLabel())
          .build());
    }

    for (Email email : contact.getEmails()) {
      emails.add(new SharedContact.Email.Builder().setValue(email.getEmail())
          .setType(localToRemoteType(email.getType()))
          .setLabel(email.getLabel())
          .build());
    }

    for (PostalAddress postalAddress : contact.getPostalAddresses()) {
      postalAddresses.add(new SharedContact.PostalAddress.Builder().setType(localToRemoteType(postalAddress.getType()))
          .setLabel(postalAddress.getLabel())
          .setStreet(postalAddress.getStreet())
          .setPobox(postalAddress.getPoBox())
          .setNeighborhood(postalAddress.getNeighborhood())
          .setCity(postalAddress.getCity())
          .setRegion(postalAddress.getRegion())
          .setPostcode(postalAddress.getPostalCode())
          .setCountry(postalAddress.getCountry())
          .build());
    }

    SharedContact.Name name = new SharedContact.Name.Builder().setDisplay(contact.getName().getDisplayName())
        .setGiven(contact.getName().getGivenName())
        .setFamily(contact.getName().getFamilyName())
        .setPrefix(contact.getName().getPrefix())
        .setSuffix(contact.getName().getSuffix())
        .setMiddle(contact.getName().getMiddleName())
        .build();

    SharedContact.Avatar avatar = null;
    if (contact.getAvatar() != null) {
      SignalServiceAttachment avatarAttachment = getAttachmentFor(context, contact.getAvatar().getImage());
      avatar = new SharedContact.Avatar.Builder().withAttachment(avatarAttachment)
          .withProfileFlag(contact.getAvatar().isProfile())
          .build();
    }

    return new SharedContact.Builder().setName(name)
                                      .withOrganization(contact.getOrganization())
                                      .withPhones(phoneNumbers)
                                      .withEmails(emails)
                                      .withAddresses(postalAddresses)
                                      .setAvatar(avatar)
                                      .build();
  }

  public static List<Contact> remoteToLocal(@NonNull List<SharedContact> sharedContacts) {
    List<Contact> contacts = new ArrayList<>(sharedContacts.size());

    for (SharedContact sharedContact : sharedContacts) {
      contacts.add(remoteToLocal(sharedContact));
    }

    return contacts;
  }

  public static Contact remoteToLocal(@NonNull SharedContact sharedContact) {
    Name name = new Name(sharedContact.getName().getDisplay().orNull(),
        sharedContact.getName().getGiven().orNull(),
        sharedContact.getName().getFamily().orNull(),
        sharedContact.getName().getPrefix().orNull(),
        sharedContact.getName().getSuffix().orNull(),
        sharedContact.getName().getMiddle().orNull());

    List<Phone> phoneNumbers = new LinkedList<>();
    if (sharedContact.getPhone().isPresent()) {
      for (SharedContact.Phone phone : sharedContact.getPhone().get()) {
        phoneNumbers.add(new Phone(phone.getValue(),
            remoteToLocalType(phone.getType()),
            phone.getLabel().orNull()));
      }
    }

    List<Email> emails = new LinkedList<>();
    if (sharedContact.getEmail().isPresent()) {
      for (SharedContact.Email email : sharedContact.getEmail().get()) {
        emails.add(new Email(email.getValue(),
            remoteToLocalType(email.getType()),
            email.getLabel().orNull()));
      }
    }

    List<PostalAddress> postalAddresses = new LinkedList<>();
    if (sharedContact.getAddress().isPresent()) {
      for (SharedContact.PostalAddress postalAddress : sharedContact.getAddress().get()) {
        postalAddresses.add(new PostalAddress(remoteToLocalType(postalAddress.getType()),
            postalAddress.getLabel().orNull(),
            postalAddress.getStreet().orNull(),
            postalAddress.getPobox().orNull(),
            postalAddress.getNeighborhood().orNull(),
            postalAddress.getCity().orNull(),
            postalAddress.getRegion().orNull(),
            postalAddress.getPostcode().orNull(),
            postalAddress.getCountry().orNull()));
      }
    }

    ContactAvatar contactAvatar = null;
    if (sharedContact.getAvatar().isPresent()) {
      // TODO: Doesn't seem to be much work here, but still might want an option not to do it
      Attachment avatarAttachment = PointerAttachment.forPointer(Optional.of(sharedContact.getAvatar().get().getAttachment())).get();
      contactAvatar = new ContactAvatar(avatarAttachment, sharedContact.getAvatar().get().isProfile());
    }

    return new Contact(name, sharedContact.getOrganization().orNull(), phoneNumbers, emails, postalAddresses, contactAvatar);
  }

  private static Phone.Type remoteToLocalType(SharedContact.Phone.Type type) {
    switch (type) {
      case HOME:   return Phone.Type.HOME;
      case MOBILE: return Phone.Type.MOBILE;
      case WORK:   return Phone.Type.WORK;
      default:     return Phone.Type.CUSTOM;
    }
  }

  private static Email.Type remoteToLocalType(SharedContact.Email.Type type) {
    switch (type) {
      case HOME:   return Email.Type.HOME;
      case MOBILE: return Email.Type.MOBILE;
      case WORK:   return Email.Type.WORK;
      default:     return Email.Type.CUSTOM;
    }
  }

  private static PostalAddress.Type remoteToLocalType(SharedContact.PostalAddress.Type type) {
    switch (type) {
      case HOME:   return PostalAddress.Type.HOME;
      case WORK:   return PostalAddress.Type.WORK;
      default:     return PostalAddress.Type.CUSTOM;
    }
  }

  private static SharedContact.Phone.Type localToRemoteType(Phone.Type type) {
    switch (type) {
      case HOME:   return SharedContact.Phone.Type.HOME;
      case MOBILE: return SharedContact.Phone.Type.MOBILE;
      case WORK:   return SharedContact.Phone.Type.WORK;
      default:     return SharedContact.Phone.Type.CUSTOM;
    }
  }

  private static SharedContact.Email.Type localToRemoteType(Email.Type type) {
    switch (type) {
      case HOME:   return SharedContact.Email.Type.HOME;
      case MOBILE: return SharedContact.Email.Type.MOBILE;
      case WORK:   return SharedContact.Email.Type.WORK;
      default:     return SharedContact.Email.Type.CUSTOM;
    }
  }

  private static SharedContact.PostalAddress.Type localToRemoteType(PostalAddress.Type type) {
    switch (type) {
      case HOME: return SharedContact.PostalAddress.Type.HOME;
      case WORK: return SharedContact.PostalAddress.Type.WORK;
      default:   return SharedContact.PostalAddress.Type.CUSTOM;
    }
  }


  private static SignalServiceAttachment getAttachmentFor(Context context, Attachment attachment) {
    try {
      if (attachment.getDataUri() == null || attachment.getSize() == 0) {
        throw new IOException("Assertion failed, outgoing attachment has no data!");
      }

      InputStream is = PartAuthority.getAttachmentStream(context, attachment.getDataUri());

      return SignalServiceAttachment.newStreamBuilder()
          .withStream(is)
          .withContentType(attachment.getContentType())
          .withLength(attachment.getSize())
          .withFileName(attachment.getFileName())
          .withVoiceNote(attachment.isVoiceNote())
          .withWidth(attachment.getWidth())
          .withHeight(attachment.getHeight())
          .withListener((total, progress) -> EventBus.getDefault().postSticky(new PartProgressEvent(attachment, total, progress)))
          .build();
    } catch (IOException ioe) {
      Log.w(TAG, "Couldn't open attachment", ioe);
    }
    return null;
  }
}
