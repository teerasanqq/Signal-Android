package org.thoughtcrime.securesms.glide;


import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import org.thoughtcrime.securesms.contacts.avatars.ContactPhoto;

import java.io.IOException;
import java.io.InputStream;

class InputStreamFetcher implements DataFetcher<InputStream> {

  private InputStream inputStream;

  InputStreamFetcher(@NonNull InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
    callback.onDataReady(inputStream);
  }

  @Override
  public void cleanup() {
    try {
      if (inputStream != null) inputStream.close();
    } catch (IOException e) {}
  }

  @Override
  public void cancel() {

  }

  @NonNull
  @Override
  public Class<InputStream> getDataClass() {
    return InputStream.class;
  }

  @NonNull
  @Override
  public DataSource getDataSource() {
    return DataSource.LOCAL;
  }
}
