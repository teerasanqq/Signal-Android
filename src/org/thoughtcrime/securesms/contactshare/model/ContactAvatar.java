package org.thoughtcrime.securesms.contactshare.model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.attachments.UriAttachment;
import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.mms.PartAuthority;
import org.thoughtcrime.securesms.util.JsonUtils;
import org.thoughtcrime.securesms.util.MediaUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class ContactAvatar implements Parcelable, Serializable {

  private final Uri     imageUri;
  private final boolean isProfile;

  public ContactAvatar(@NonNull Uri imageUri, boolean isProfile) {
    this.imageUri  = imageUri;
    this.isProfile = isProfile;
  }

  private ContactAvatar(Parcel in) {
    this(in.readParcelable(Uri.class.getClassLoader()), in.readByte() != 0);
  }

  public @NonNull InputStream getImageStream(@NonNull Context context) throws IOException {
    return PartAuthority.getAttachmentStream(context, imageUri);
  }

  public boolean isProfile() {
    return isProfile;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(imageUri, flags);
    dest.writeByte((byte) (isProfile ? 1 : 0));
  }

  public static final Creator<ContactAvatar> CREATOR = new Creator<ContactAvatar>() {
    @Override
    public ContactAvatar createFromParcel(Parcel in) {
      return new ContactAvatar(in);
    }

    @Override
    public ContactAvatar[] newArray(int size) {
      return new ContactAvatar[size];
    }
  };
}
