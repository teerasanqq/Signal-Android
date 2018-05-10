package org.thoughtcrime.securesms.contactshare.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ContactWithAvatar implements Parcelable {

  private final Contact contact;
  private final Uri avatarUri;

  public ContactWithAvatar(@NonNull Contact contact, @Nullable Uri avatarUri) {
    this.contact = contact;
    this.avatarUri = avatarUri;

    validateContactConsistency();
  }

  private ContactWithAvatar(Parcel in) {
    this(in.readParcelable(Contact.class.getClassLoader()),
         in.readParcelable(Uri.class.getClassLoader()));
  }

  private void validateContactConsistency() {
    if (contact.getAvatarState() != Contact.AvatarState.NONE && avatarUri == null) {
      throw new IllegalStateException("The contact state indicates an avatar should be present, but none is.");
    } else if (contact.getAvatarState() == Contact.AvatarState.NONE && avatarUri != null) {
      throw new IllegalStateException("The contact state indicates there's no avatar, but one was provided.");
    }
  }

  public @NonNull Contact getContact() {
    return contact;
  }

  public Uri getAvatarUri() {
    return avatarUri;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(contact, flags);
    dest.writeParcelable(avatarUri, flags);
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
