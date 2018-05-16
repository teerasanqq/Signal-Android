package org.thoughtcrime.securesms;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.thoughtcrime.securesms.contactshare.Contact;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.MmsMessageRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.mms.SharedContactSlide;
import org.thoughtcrime.securesms.recipients.Recipient;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface BindableConversationItem extends Unbindable {
  void bind(@NonNull MessageRecord      messageRecord,
            @NonNull GlideRequests      glideRequests,
            @NonNull Locale             locale,
            @NonNull Set<MessageRecord> batchSelected,
            @NonNull Recipient          recipients,
                     boolean            pulseHighlight);

  MessageRecord getMessageRecord();

  void setEventListener(@Nullable EventListener listener);

  interface EventListener {
    void onQuoteClicked(MmsMessageRecord messageRecord);
    void onSharedContactDetailsClicked(@NonNull SharedContactSlide sharedContactSlide, @NonNull View avatarTransitionView);
    void onAddToContactsClicked(@NonNull SharedContactSlide sharedContactSlide, @NonNull Contact sharedContact);
    void onMessageSharedContactClicked(@NonNull List<Recipient> choices);
    void onInviteSharedContactClicked(@NonNull List<Recipient> choices);
  }
}
