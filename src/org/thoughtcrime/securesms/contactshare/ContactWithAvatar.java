package org.thoughtcrime.securesms.contactshare;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.attachments.UriAttachment;
import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.util.MediaUtil;

// TODO(greyson): Revisit this model to see if we can merge it back into Contact
public class ContactWithAvatar implements Parcelable {

  private final Contact    contact;
  private final Attachment avatarAttachment;

  public ContactWithAvatar(@NonNull Contact contact, @Nullable Uri avatarUri) {
    this(contact, attachmentFromUri(avatarUri));
  }

  public ContactWithAvatar(@NonNull Contact contact, @Nullable Attachment avatarAttachment) {
    this.contact          = contact;
    this.avatarAttachment = avatarAttachment;

    validateContactConsistency();
  }

  private static Attachment attachmentFromUri(@Nullable Uri uri) {
    if (uri == null) return null;
    return new UriAttachment(uri, MediaUtil.IMAGE_JPEG, AttachmentDatabase.TRANSFER_PROGRESS_DONE, 0, null, false, false);
  }

  private ContactWithAvatar(Parcel in) {
    this(in.readParcelable(Contact.class.getClassLoader()),
        (Uri) in.readParcelable(Uri.class.getClassLoader()));
  }

  private void validateContactConsistency() {
    if (contact.getAvatarState() != Contact.AvatarState.NONE && avatarAttachment == null) {
      throw new IllegalStateException("The contact state indicates an avatar should be present, but none is.");
    } else if (contact.getAvatarState() == Contact.AvatarState.NONE && avatarAttachment != null) {
      throw new IllegalStateException("The contact state indicates there's no avatar, but one was provided.");
    }
  }

  public @NonNull Contact getContact() {
    return contact;
  }

  public @Nullable Attachment getAvatarAttachment() {
    return avatarAttachment;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(contact, flags);
    dest.writeParcelable(avatarAttachment.getDataUri(), flags);
  }

  public static final Creator<ContactWithAvatar> CREATOR = new Creator<ContactWithAvatar>() {
    @Override
    public ContactWithAvatar createFromParcel(Parcel in) {
      return new ContactWithAvatar(in);
    }

    @Override
    public ContactWithAvatar[] newArray(int size) {
      return new ContactWithAvatar[size];
    }
  };
}
