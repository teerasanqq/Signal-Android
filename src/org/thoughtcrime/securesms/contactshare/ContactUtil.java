package org.thoughtcrime.securesms.contactshare;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.ContactStream;
import org.thoughtcrime.securesms.contactshare.model.Email;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.contactshare.model.PostalAddress;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ContactUtil {

  private static final String TAG = ContactUtil.class.getSimpleName();

  public static long getContactIdFromUri(@NonNull Uri uri) {
    try {
      return Long.parseLong(uri.getLastPathSegment());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public static @NonNull String getDisplayName(@Nullable Contact contact) {
    if (contact == null) {
      return "";
    }

    if (!TextUtils.isEmpty(contact.getName().getDisplayName())) {
      return contact.getName().getDisplayName();
    }

    if (!TextUtils.isEmpty(contact.getOrganization())) {
      return contact.getOrganization();
    }

    return "";
  }

  public static @Nullable Phone getDisplayNumber(@NonNull Contact contact) {
    return getDisplayNumber(new ContactRepository.ContactInfo(contact));
  }

  public static @Nullable Phone getDisplayNumber(@NonNull ContactRepository.ContactInfo contactInfo) {
    Contact contact = contactInfo.getContact();

    if (contact.getPhoneNumbers().size() == 0) {
      return null;
    }

    List<Phone> signalNumbers = Stream.of(contact.getPhoneNumbers()).filter(contactInfo::isPush).toList();
    if (signalNumbers.size() > 0) {
      return signalNumbers.get(0);
    }

    List<Phone> mobileNumbers = Stream.of(contact.getPhoneNumbers()).filter(number -> number.getType() == Phone.Type.MOBILE).toList();
    if (mobileNumbers.size() > 0) {
      return mobileNumbers.get(0);
    }

    return contact.getPhoneNumbers().get(0);
  }

  public static @NonNull String getPrettyPhoneNumber(@NonNull Phone phoneNumber, @NonNull Locale fallbackLocale) {
    PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    try {
      PhoneNumber parsed = util.parse(phoneNumber.getNumber(), fallbackLocale.getISO3Country());
      return util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    } catch (NumberParseException e) {
      return phoneNumber.getNumber();
    }
  }

  public static @NonNull String getNormalizedPhoneNumber(@NonNull Context context, @NonNull String number) {
    Address address = Address.fromExternal(context, number);
    return address.serialize();
  }

  public static @NonNull String getLocalPhoneNumber(@NonNull String number, @NonNull Locale fallbackLocale) {
    PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    try {
      PhoneNumber parsed = util.parse(number, fallbackLocale.getISO3Country());
      return String.valueOf(parsed.getNationalNumber());
    } catch (NumberParseException e) {
      return number;
    }
  }

  public static void selectRecipient(@NonNull Context context, @NonNull List<Recipient> choices, @NonNull RecipientSelectedCallback callback) {
    if (choices.size() > 1) {
      CharSequence[] values = new CharSequence[choices.size()];

      for (int i = 0; i < values.length; i++) {
        // TODO(greyson): Make pretty?
        values[i] = choices.get(i).getAddress().toPhoneString();
      }

      new AlertDialog.Builder(context)
                     .setItems(values, ((dialog, which) -> callback.onSelected(choices.get(which))))
                     .create()
                     .show();
    } else {
      callback.onSelected(choices.get(0));
    }
  }

  public static List<Recipient> getRecipients(@NonNull Context context, @NonNull Contact contact) {
    return Stream.of(contact.getPhoneNumbers()).map(phone -> Recipient.from(context, Address.fromExternal(context, phone.getNumber()), true)).toList();
  }

  public static @NonNull Intent buildAddToContactsIntent(@NonNull Contact contact, @Nullable byte[] avatarBytes) {
    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
    intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);

    if (!TextUtils.isEmpty(contact.getOrganization())) {
      intent.putExtra(ContactsContract.Intents.Insert.COMPANY, contact.getOrganization());
    }

    if (contact.getPhoneNumbers().size() > 0) {
      intent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.getPhoneNumbers().get(0).getNumber());
      intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, getSystemType(contact.getPhoneNumbers().get(0).getType()));
    }

    if (contact.getPhoneNumbers().size() > 1) {
      intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, contact.getPhoneNumbers().get(1).getNumber());
      intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, getSystemType(contact.getPhoneNumbers().get(1).getType()));
    }

    if (contact.getPhoneNumbers().size() > 2) {
      intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, contact.getPhoneNumbers().get(2).getNumber());
      intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, getSystemType(contact.getPhoneNumbers().get(2).getType()));
    }

    if (contact.getEmails().size() > 0) {
      intent.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.getEmails().get(0).getEmail());
      intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, getSystemType(contact.getEmails().get(0).getType()));
    }

    if (contact.getEmails().size() > 1) {
      intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, contact.getEmails().get(1).getEmail());
      intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, getSystemType(contact.getEmails().get(1).getType()));
    }

    if (contact.getEmails().size() > 2) {
      intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, contact.getEmails().get(2).getEmail());
      intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, getSystemType(contact.getEmails().get(2).getType()));
    }

    if (contact.getPostalAddresses().size() > 0) {
      intent.putExtra(ContactsContract.Intents.Insert.POSTAL, contact.getPostalAddresses().get(0).toString());
      intent.putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, getSystemType(contact.getPostalAddresses().get(0).getType()));
    }

    if (avatarBytes != null) {
      ArrayList<ContentValues> valuesArray = new ArrayList<>(1);
      ContentValues values = new ContentValues();
      valuesArray.add(values);

      values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
      values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, avatarBytes);
      intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, valuesArray);
    }
    return intent;
  }

  private static int getSystemType(Phone.Type type) {
    switch (type) {
      case HOME:   return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
      case MOBILE: return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
      case WORK:   return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
      default:     return ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
    }
  }

  private static int getSystemType(Email.Type type) {
    switch (type) {
      case HOME:   return ContactsContract.CommonDataKinds.Email.TYPE_HOME;
      case MOBILE: return ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
      case WORK:   return ContactsContract.CommonDataKinds.Email.TYPE_WORK;
      default:     return ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM;
    }
  }

  private static int getSystemType(PostalAddress.Type type) {
    switch (type) {
      case HOME: return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
      case WORK: return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
      default:   return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
    }
  }

  public interface RecipientSelectedCallback {
    void onSelected(@NonNull Recipient recipient);
  }
}
