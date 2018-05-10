package org.thoughtcrime.securesms.contactshare.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.io.Serializable;

public class Email implements Selectable, Parcelable, Serializable {

  private static final long serialVersionUID = 1L;

  private final String email;
  private final Type   type;
  private final String label;

  private boolean selected;

  public Email(@NonNull String email, @NonNull Type type, @Nullable String label) {
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
