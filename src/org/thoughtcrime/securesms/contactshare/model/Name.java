package org.thoughtcrime.securesms.contactshare.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Objects;

import java.io.Serializable;

public class Name implements Parcelable, Serializable {

  private static final long serialVersionUID = 1L;

  private final String displayName;
  private final String givenName;
  private final String familyName;
  private final String prefix;
  private final String suffix;
  private final String middleName;

  public Name(@Nullable String displayName,
              @Nullable String givenName,
              @Nullable String familyName,
              @Nullable String prefix,
              @Nullable String suffix,
              @Nullable String middleName)
  {
    this.displayName = displayName;
    this.givenName  = givenName;
    this.familyName = familyName;
    this.prefix     = prefix;
    this.suffix     = suffix;
    this.middleName = middleName;
  }

  Name(Parcel in) {
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
