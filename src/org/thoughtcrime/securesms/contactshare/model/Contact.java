package org.thoughtcrime.securesms.contactshare.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Contact implements Parcelable, Serializable {

  private static final long serialVersionUID = 1L;

  private final Name                name;
  private final String              organization;
  private final List<Phone>         phoneNumbers;
  private final List<Email>         emails;
  private final List<PostalAddress> postalAddresses;
  private final AvatarState         avatarState;
  private final int                 avatarSize;

  public Contact(@NonNull  Name                name,
                 @Nullable String              organization,
                 @NonNull  List<Phone>         phoneNumbers,
                 @NonNull  List<Email>         emails,
                 @NonNull  List<PostalAddress> postalAddresses,
                 @NonNull  AvatarState         avatarState,
                           int                 avatarSize)
  {
    this.name            = name;
    this.organization    = organization;
    this.phoneNumbers    = Collections.unmodifiableList(phoneNumbers);
    this.emails          = Collections.unmodifiableList(emails);
    this.postalAddresses = Collections.unmodifiableList(postalAddresses);
    this.avatarState     = avatarState;
    this.avatarSize      = avatarSize;
  }

  private Contact(Parcel in) {
    this(in.readParcelable(Name.class.getClassLoader()),
         in.readString(),
         in.createTypedArrayList(Phone.CREATOR),
         in.createTypedArrayList(Email.CREATOR),
         in.createTypedArrayList(PostalAddress.CREATOR),
         AvatarState.valueOf(in.readString()),
         in.readInt());
  }

  public @NonNull Name getName() {
    return name;
  }

  public @Nullable String getOrganization() {
    return organization;
  }

  public @NonNull List<Phone> getPhoneNumbers() {
    return phoneNumbers;
  }

  public @NonNull List<Email> getEmails() {
    return emails;
  }

  public @NonNull List<PostalAddress> getPostalAddresses() {
    return postalAddresses;
  }

  public @NonNull AvatarState getAvatarState() {
    return avatarState;
  }

  public int getAvatarSize() {
    return avatarSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Contact contact = (Contact) o;

    if (avatarSize != contact.avatarSize) return false;
    if (!name.equals(contact.name)) return false;
    if (organization != null ? !organization.equals(contact.organization) : contact.organization != null)
      return false;
    if (!phoneNumbers.equals(contact.phoneNumbers)) return false;
    if (!emails.equals(contact.emails)) return false;
    if (!postalAddresses.equals(contact.postalAddresses)) return false;
    return avatarState == contact.avatarState;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (organization != null ? organization.hashCode() : 0);
    result = 31 * result + phoneNumbers.hashCode();
    result = 31 * result + emails.hashCode();
    result = 31 * result + postalAddresses.hashCode();
    result = 31 * result + avatarState.hashCode();
    result = 31 * result + avatarSize;
    return result;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(name, flags);
    dest.writeString(organization);
    dest.writeTypedList(phoneNumbers);
    dest.writeTypedList(emails);
    dest.writeTypedList(postalAddresses);
    dest.writeString(avatarState.name());
    dest.writeInt(avatarSize);
  }

  public static final Creator<Contact> CREATOR = new Creator<Contact>() {
    @Override
    public Contact createFromParcel(Parcel in) {
      return new Contact(in);
    }

    @Override
    public Contact[] newArray(int size) {
      return new Contact[size];
    }
  };

  public enum AvatarState {

    NONE(false), PROFILE(true), SYSTEM(false);

    private final boolean isProfile;

    AvatarState(boolean isProfile) {
      this.isProfile = isProfile;
    }

    public boolean isProfile() {
      return isProfile;
    }
  }
}
