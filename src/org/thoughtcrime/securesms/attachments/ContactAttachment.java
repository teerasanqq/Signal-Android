package org.thoughtcrime.securesms.attachments;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.database.AttachmentDatabase;
import org.thoughtcrime.securesms.util.MediaUtil;

public class ContactAttachment extends Attachment {

  private final Uri uri;

  public ContactAttachment(@Nullable Uri uri) {
    super(MediaUtil.SHARED_CONTACT, AttachmentDatabase.TRANSFER_PROGRESS_DONE, 0, null, null, null, null, null, null, false, 0, 0, false);

    this.uri = uri;
  }

  public ContactAttachment(@Nullable Uri uri, @NonNull Attachment avatarAttachment)
  {
    super(MediaUtil.SHARED_CONTACT,
          avatarAttachment.getTransferState(),
          avatarAttachment.getSize(),
          avatarAttachment.getFileName(),
          avatarAttachment.getLocation(),
          avatarAttachment.getKey(),
          avatarAttachment.getRelay(),
          avatarAttachment.getDigest(),
          avatarAttachment.getFastPreflightId(),
          false,
          avatarAttachment.getWidth(),
          avatarAttachment.getHeight(),
          false);

    this.uri = uri;
  }

  @Override
  public @Nullable Uri getDataUri() {
    return uri;
  }

  @Override
  public @Nullable Uri getThumbnailUri() {
    return null;
  }
}
