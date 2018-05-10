package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.util.MediaUtil;

public class SharedContactSlide extends ImageSlide {

  public SharedContactSlide(Context context, Attachment attachment) {
    super(context, attachment);
  }

  public SharedContactSlide(Context context, Uri uri, long size, int width, int height) {
    super(context, constructAttachmentFromUri(context, uri, MediaUtil.IMAGE_GIF, size, width, height, true, null, false, false));
  }

  @Override
  public boolean hasSharedContact() {
    return true;
  }
}
