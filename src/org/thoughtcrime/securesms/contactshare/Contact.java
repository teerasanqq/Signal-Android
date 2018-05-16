package org.thoughtcrime.securesms.contactshare;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONException;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.attachments.AttachmentId;
import org.thoughtcrime.securesms.util.JsonUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Contact implements Parcelable {

  private static final String TAG = Contact.class.getSimpleName();

  @JsonProperty
  private final Name                name;

  @JsonProperty
  private final String              organization;

  @JsonProperty
  private final List<Phone>         phoneNumbers;

  @JsonProperty
  private final List<Email>         emails;

  @JsonProperty
  private final List<PostalAddress> postalAddresses;

  @JsonProperty
  private final AvatarState         avatarState;

  @JsonProperty
  private final int                 avatarSize;

  @JsonProperty
  private final AttachmentId        attachmentId;

  public Contact(@JsonProperty("name")            @NonNull  Name                name,
                 @JsonProperty("organization")    @Nullable String              organization,
                 @JsonProperty("phoneNumbers")    @NonNull  List<Phone>         phoneNumbers,
                 @JsonProperty("emails")          @NonNull  List<Email>         emails,
                 @JsonProperty("postalAddresses") @NonNull  List<PostalAddress> postalAddresses,
                 @JsonProperty("avatarState")     @NonNull  AvatarState         avatarState,
                 @JsonProperty("avatarSize")                int                 avatarSize,
                 @JsonProperty("attachmentId")    @Nullable AttachmentId        attachmentId)
  {
    this.name            = name;
    this.organization    = organization;
    this.phoneNumbers    = Collections.unmodifiableList(phoneNumbers);
    this.emails          = Collections.unmodifiableList(emails);
    this.postalAddresses = Collections.unmodifiableList(postalAddresses);
    this.avatarState     = avatarState;
    this.avatarSize      = avatarSize;
    this.attachmentId    = attachmentId;
  }

  private Contact(Parcel in) {
    this(in.readParcelable(Name.class.getClassLoader()),
         in.readString(),
         in.createTypedArrayList(Phone.CREATOR),
         in.createTypedArrayList(Email.CREATOR),
         in.createTypedArrayList(PostalAddress.CREATOR),
         AvatarState.valueOf(in.readString()),
         in.readInt(),
         null);
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

  public @Nullable AttachmentId getAttachmentId() {
    return attachmentId;
  }

  public String serialize() {
    try {
      return JsonUtils.toJson(this);
    } catch (IOException e) {
      Log.w(TAG, "Failed to serialize to JSON.", e);
      return "";
    }
  }

  public static Contact deserialize(@NonNull String serialized) throws IOException {
    return JsonUtils.fromJson(serialized, Contact.class);
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

  public static class Name implements Parcelable {

    @JsonProperty
    private final String displayName;

    @JsonProperty
    private final String givenName;

    @JsonProperty
    private final String familyName;

    @JsonProperty
    private final String prefix;

    @JsonProperty
    private final String suffix;

    @JsonProperty
    private final String middleName;

    Name(@JsonProperty("displayName") @Nullable String displayName,
         @JsonProperty("givenName")   @Nullable String givenName,
         @JsonProperty("familyName")  @Nullable String familyName,
         @JsonProperty("prefix")      @Nullable String prefix,
         @JsonProperty("suffix")      @Nullable String suffix,
         @JsonProperty("middleName")  @Nullable String middleName)
    {
      this.displayName = displayName;
      this.givenName  = givenName;
      this.familyName = familyName;
      this.prefix     = prefix;
      this.suffix     = suffix;
      this.middleName = middleName;
    }

    private Name(Parcel in) {
      this(in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
    }

    public @Nullable String getDisplayName() {
      return displayName;
    }

    public @Nullable String getGivenName() {
      return givenName;
    }

    public @Nullable String getFamilyName() {
      return familyName;
    }

    public @Nullable String getPrefix() {
      return prefix;
    }

    public @Nullable String getSuffix() {
      return suffix;
    }

    public @Nullable String getMiddleName() {
      return middleName;
    }

    public boolean isEmpty() {
      return TextUtils.isEmpty(displayName) &&
             TextUtils.isEmpty(givenName)   &&
             TextUtils.isEmpty(familyName)  &&
             TextUtils.isEmpty(prefix)      &&
             TextUtils.isEmpty(suffix)      &&
             TextUtils.isEmpty(middleName);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Name name = (Name) o;

      if (displayName != null ? !displayName.equals(name.displayName) : name.displayName != null)
        return false;
      if (givenName != null ? !givenName.equals(name.givenName) : name.givenName != null)
        return false;
      if (familyName != null ? !familyName.equals(name.familyName) : name.familyName != null)
        return false;
      if (prefix != null ? !prefix.equals(name.prefix) : name.prefix != null) return false;
      if (suffix != null ? !suffix.equals(name.suffix) : name.suffix != null) return false;
      return middleName != null ? middleName.equals(name.middleName) : name.middleName == null;
    }

    @Override
    public int hashCode() {
      int result = displayName != null ? displayName.hashCode() : 0;
      result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
      result = 31 * result + (familyName != null ? familyName.hashCode() : 0);
      result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
      result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
      result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
      return result;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(displayName);
      dest.writeString(givenName);
      dest.writeString(familyName);
      dest.writeString(prefix);
      dest.writeString(suffix);
      dest.writeString(middleName);
    }

    public static final Creator<Name> CREATOR = new Creator<Name>() {
      @Override
      public Name createFromParcel(Parcel in) {
        return new Name(in);
      }

      @Override
      public Name[] newArray(int size) {
        return new Name[size];
      }
    };
  }

  public static class Phone implements Selectable, Parcelable {

    @JsonProperty
    private final String number;

    @JsonProperty
    private final Type   type;

    @JsonProperty
    private final String label;

    private boolean selected;

    Phone(@JsonProperty("number") @NonNull  String number,
          @JsonProperty("type")   @NonNull  Type   type,
          @JsonProperty("label")  @Nullable String label)
    {
      this.number   = number;
      this.type     = type;
      this.label    = label;
      this.selected = true;
    }

    private Phone(Parcel in) {
      this(in.readString(), Type.valueOf(in.readString()), in.readString());
    }

    public @NonNull String getNumber() {
      return number;
    }

    public @NonNull Type getType() {
      return type;
    }

    public @Nullable String getLabel() {
      return label;
    }

    @Override
    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    @Override
    public boolean isSelected() {
      return selected;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Phone phone = (Phone) o;

      if (!number.equals(phone.number)) return false;
      if (type != phone.type) return false;
      return label != null ? label.equals(phone.label) : phone.label == null;
    }

    @Override
    public int hashCode() {
      int result = number.hashCode();
      result = 31 * result + type.hashCode();
      result = 31 * result + (label != null ? label.hashCode() : 0);
      return result;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(number);
      dest.writeString(type.name());
      dest.writeString(label);
    }

    public static final Creator<Phone> CREATOR = new Creator<Phone>() {
      @Override
      public Phone createFromParcel(Parcel in) {
        return new Phone(in);
      }

      @Override
      public Phone[] newArray(int size) {
        return new Phone[size];
      }
    };

    public enum Type {
      HOME, MOBILE, WORK, CUSTOM
    }
  }

  public static class Email implements Selectable, Parcelable {

    @JsonProperty
    private final String email;

    @JsonProperty
    private final Type   type;

    @JsonProperty
    private final String label;

    private boolean selected;

    Email(@JsonProperty("email") @NonNull  String email,
          @JsonProperty("type")  @NonNull  Type   type,
          @JsonProperty("label") @Nullable String label)
    {
      this.email    = email;
      this.type     = type;
      this.label    = label;
      this.selected = true;
    }

    private Email(Parcel in) {
      this(in.readString(), Type.valueOf(in.readString()), in.readString());
    }

    public @NonNull String getEmail() {
      return email;
    }

    public @NonNull Type getType() {
      return type;
    }

    public @NonNull String getLabel() {
      return label;
    }

    @Override
    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    @Override
    public boolean isSelected() {
      return selected;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Email email1 = (Email) o;

      if (!email.equals(email1.email)) return false;
      if (type != email1.type) return false;
      return label != null ? label.equals(email1.label) : email1.label == null;
    }

    @Override
    public int hashCode() {
      int result = email.hashCode();
      result = 31 * result + type.hashCode();
      result = 31 * result + (label != null ? label.hashCode() : 0);
      return result;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(email);
      dest.writeString(type.name());
      dest.writeString(label);
    }

    public static final Creator<Email> CREATOR = new Creator<Email>() {
      @Override
      public Email createFromParcel(Parcel in) {
        return new Email(in);
      }

      @Override
      public Email[] newArray(int size) {
        return new Email[size];
      }
    };

    public enum Type {
      HOME, MOBILE, WORK, CUSTOM
    }
  }

  public static class PostalAddress implements Selectable, Parcelable {

    @JsonProperty
    private final Type   type;

    @JsonProperty
    private final String label;

    @JsonProperty
    private final String street;

    @JsonProperty
    private final String poBox;

    @JsonProperty
    private final String neighborhood;

    @JsonProperty
    private final String city;

    @JsonProperty
    private final String region;

    @JsonProperty
    private final String postalCode;

    @JsonProperty
    private final String country;

    private boolean selected;

    PostalAddress(@JsonProperty("type")         @NonNull  Type   type,
                  @JsonProperty("label")        @Nullable String label,
                  @JsonProperty("street")       @Nullable String street,
                  @JsonProperty("poBox")        @Nullable String poBox,
                  @JsonProperty("neighborhood") @Nullable String neighborhood,
                  @JsonProperty("city")         @Nullable String city,
                  @JsonProperty("region")       @Nullable String region,
                  @JsonProperty("postalCode")   @Nullable String postalCode,
                  @JsonProperty("country")      @Nullable String country)
    {
      this.type         = type;
      this.label        = label;
      this.street       = street;
      this.poBox        = poBox;
      this.neighborhood = neighborhood;
      this.city         = city;
      this.region       = region;
      this.postalCode   = postalCode;
      this.country      = country;
      this.selected     = true;
    }

    private PostalAddress(Parcel in) {
      this(Type.valueOf(in.readString()),
          in.readString(),
          in.readString(),
          in.readString(),
          in.readString(),
          in.readString(),
          in.readString(),
          in.readString(),
          in.readString());
    }

    public @NonNull Type getType() {
      return type;
    }

    public @Nullable String getLabel() {
      return label;
    }

    public @Nullable String getStreet() {
      return street;
    }

    public @Nullable String getPoBox() {
      return poBox;
    }

    public @Nullable String getNeighborhood() {
      return neighborhood;
    }

    public @Nullable String getCity() {
      return city;
    }

    public @Nullable String getRegion() {
      return region;
    }

    public @Nullable String getPostalCode() {
      return postalCode;
    }

    public @Nullable String getCountry() {
      return country;
    }

    @Override
    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    @Override
    public boolean isSelected() {
      return selected;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PostalAddress that = (PostalAddress) o;

      if (type != that.type) return false;
      if (label != null ? !label.equals(that.label) : that.label != null) return false;
      if (street != null ? !street.equals(that.street) : that.street != null) return false;
      if (poBox != null ? !poBox.equals(that.poBox) : that.poBox != null) return false;
      if (neighborhood != null ? !neighborhood.equals(that.neighborhood) : that.neighborhood != null)
        return false;
      if (city != null ? !city.equals(that.city) : that.city != null) return false;
      if (region != null ? !region.equals(that.region) : that.region != null) return false;
      if (postalCode != null ? !postalCode.equals(that.postalCode) : that.postalCode != null)
        return false;
      return country != null ? country.equals(that.country) : that.country == null;
    }

    @Override
    public int hashCode() {
      int result = type != null ? type.hashCode() : 0;
      result = 31 * result + (label != null ? label.hashCode() : 0);
      result = 31 * result + (street != null ? street.hashCode() : 0);
      result = 31 * result + (poBox != null ? poBox.hashCode() : 0);
      result = 31 * result + (neighborhood != null ? neighborhood.hashCode() : 0);
      result = 31 * result + (city != null ? city.hashCode() : 0);
      result = 31 * result + (region != null ? region.hashCode() : 0);
      result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
      result = 31 * result + (country != null ? country.hashCode() : 0);
      return result;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(type.name());
      dest.writeString(label);
      dest.writeString(street);
      dest.writeString(poBox);
      dest.writeString(neighborhood);
      dest.writeString(city);
      dest.writeString(region);
      dest.writeString(postalCode);
      dest.writeString(country);
    }

    public static final Creator<PostalAddress> CREATOR = new Creator<PostalAddress>() {
      @Override
      public PostalAddress createFromParcel(Parcel in) {
        return new PostalAddress(in);
      }

      @Override
      public PostalAddress[] newArray(int size) {
        return new PostalAddress[size];
      }
    };

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();

      if (!TextUtils.isEmpty(street)) {
        builder.append(street).append('\n');
      }

      if (!TextUtils.isEmpty(poBox)) {
        builder.append(poBox).append('\n');
      }

      if (!TextUtils.isEmpty(neighborhood)) {
        builder.append(neighborhood).append('\n');
      }

      if (!TextUtils.isEmpty(city) && !TextUtils.isEmpty(region)) {
        builder.append(city).append(", ").append(region);
      } else if (!TextUtils.isEmpty(city)) {
        builder.append(city).append(' ');
      } else if (!TextUtils.isEmpty(region)) {
        builder.append(region).append(' ');
      }

      if (!TextUtils.isEmpty(postalCode)) {
        builder.append(postalCode);
      }

      if (!TextUtils.isEmpty(country)) {
        builder.append('\n').append(country);
      }

      return builder.toString().trim();
    }

    public enum Type {
      HOME, WORK, CUSTOM
    }
  }
}
