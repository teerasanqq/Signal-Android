package org.thoughtcrime.securesms.contactshare.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Objects;

import java.io.Serializable;

public class PostalAddress implements Selectable, Parcelable, Serializable {

  private static final long serialVersionUID = 1L;

  private final Type   type;
  private final String label;
  private final String street;
  private final String poBox;
  private final String neighborhood;
  private final String city;
  private final String region;
  private final String postalCode;
  private final String country;

  private boolean selected;

  public PostalAddress(@NonNull  Type   type,
                       @Nullable String label,
                       @Nullable String street,
                       @Nullable String poBox,
                       @Nullable String neighborhood,
                       @Nullable String city,
                       @Nullable String region,
                       @Nullable String postalCode,
                       @Nullable String country)
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
