package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.database.RecipientDatabase;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.mms.SharedContactSlide;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientModifiedListener;
import org.thoughtcrime.securesms.util.CommunicationActions;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedContactDetailsActivity extends PassphraseRequiredActionBarActivity
                                          implements SharedContactInjector.Target, RecipientModifiedListener
{

  private static final String TAG = SharedContactDetailsActivity.class.getSimpleName();

  private static final int    CODE_ADD_EDIT_CONTACT = 2323;
  private static final String KEY_CONTACT_URI       = "contact_uri";

  private ContactFieldAdapter contactFieldAdapter;
  private TextView            nameView;
  private TextView            numberView;
  private ImageView           avatarView;
  private View                addButtonView;
  private View                inviteButtonView;
  private ViewGroup           engageContainerView;
  private View                messageButtonView;
  private View                callButtonView;

  private GlideRequests       glideRequests;
  private Contact             contact;
  private Uri                 contactSlideUri;

  private final DynamicTheme    dynamicTheme    = new DynamicNoActionBarTheme();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  private final Map<String, Recipient> activeRecipients = new HashMap<>();

  public static Intent getIntent(@NonNull Context context, @NonNull SharedContactSlide sharedContactSlide) {
    if (sharedContactSlide.getUri() == null) {
      throw new IllegalStateException("Slide must have a Uri.");
    }

    Intent intent = new Intent(context, SharedContactDetailsActivity.class);
    intent.putExtra(KEY_CONTACT_URI, sharedContactSlide.getUri());
    return intent;
  }

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState, boolean ready) {
    setContentView(R.layout.activity_shared_contact_details);

    if (getIntent() == null) {
      throw new IllegalStateException("You must supply arguments to this activity. Please use the #newInstance() method.");
    }

    contactSlideUri = getIntent().getParcelableExtra(KEY_CONTACT_URI);
    if (contactSlideUri == null) {
      throw new IllegalStateException("You must supply a ContactSlide Uri to this fragment. Please use the #newInstance() method.");
    }

    initToolbar();
    initViews();

    SharedContactInjector.getInstance(this).load(contactSlideUri, this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onCreate(this);
    dynamicTheme.onResume(this);
  }

  private void initToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setLogo(null);
    getSupportActionBar().setTitle("");
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      int[]      attrs = {R.attr.shared_contact_details_titlebar};
      TypedArray array = obtainStyledAttributes(attrs);
      int        color = array.getResourceId(0, android.R.color.black);

      array.recycle();

      getWindow().setStatusBarColor(getResources().getColor(color));
    }
  }

  private void initViews() {
    nameView            = findViewById(R.id.contact_details_name);
    numberView          = findViewById(R.id.contact_details_number);
    avatarView          = findViewById(R.id.contact_details_avatar);
    addButtonView       = findViewById(R.id.contact_details_add_button);
    inviteButtonView    = findViewById(R.id.contact_details_invite_button);
    engageContainerView = findViewById(R.id.contact_details_engage_container);
    messageButtonView   = findViewById(R.id.contact_details_message_button);
    callButtonView      = findViewById(R.id.contact_details_call_button);

    contactFieldAdapter = new ContactFieldAdapter(dynamicLanguage.getCurrentLocale(), false);

    RecyclerView list = findViewById(R.id.contact_details_fields);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(contactFieldAdapter);

    glideRequests = GlideApp.with(this);
  }

  @Override
  public void onModified(Recipient recipient) {
    presentActionButtons(Collections.singletonList(recipient));
  }

  @Override
  public void setContact(@Nullable Contact contact) {
    this.contact = contact;

    if (contact != null) {
      nameView.setText(ContactUtil.getDisplayName(contact));

      Phone displayNumber = ContactUtil.getDisplayNumber(contact);

      // TODO(greyson): Make one big util?
      if (displayNumber != null) {
        numberView.setText(ContactUtil.getPrettyPhoneNumber(displayNumber, dynamicLanguage.getCurrentLocale()));
      } else if (contact.getEmails().size() > 0) {
        numberView.setText(contact.getEmails().get(0).getEmail());
      } else {
        numberView.setText("");
      }

      addButtonView.setOnClickListener(v -> {
        new BuildAddToContactsIntentTask(this, contactSlideUri, intent -> {
          if (intent != null) {
            startActivityForResult(intent, CODE_ADD_EDIT_CONTACT);
          } else {
            Log.w(TAG, "Failed to create an intent to add a contact.");
          }
        }).execute();
      });

      presentActionButtons(ContactUtil.getRecipients(this, contact));
      contactFieldAdapter.setFields(this, contact.getPhoneNumbers(), contact.getEmails(), contact.getPostalAddresses());
    } else {
      nameView.setText("");
      numberView.setText("");
    }
  }

  @Override
  public void setAvatar(@Nullable InputStream inputStream) {
    if (inputStream != null) {
      glideRequests.load(inputStream)
          .fallback(R.drawable.ic_contact_picture)
          .circleCrop()
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .into(avatarView);
    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
          .circleCrop()
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .into(avatarView);
    }
  }

  private void presentActionButtons(@NonNull List<Recipient> recipients) {
    for (Recipient recipient : recipients) {
      activeRecipients.put(recipient.getAddress().serialize(), recipient);
    }

    // TODO(greyson): Make one forloop to go through this
    Stream.of(activeRecipients.values()).forEach(recipient -> { recipient.removeListener(this); recipient.addListener(this);});

    List<Recipient> pushUsers   = Stream.of(activeRecipients.values())
        .filter(recipient -> recipient.getRegistered() == RecipientDatabase.RegisteredState.REGISTERED)
        .toList();

    List<Recipient> systemUsers = Stream.of(activeRecipients.values())
        .filter(recipient -> recipient.getRegistered() != RecipientDatabase.RegisteredState.REGISTERED && recipient.isSystemContact())
        .toList();

    if (!pushUsers.isEmpty()) {
      engageContainerView.setVisibility(View.VISIBLE);
      inviteButtonView.setVisibility(View.GONE);

      messageButtonView.setOnClickListener(v -> {
        ContactUtil.selectRecipient(this, pushUsers, recipient -> {
          new RetrieveThreadIdTask(this, recipient, threadId -> {
            CommunicationActions.startConversation(this, recipient.getAddress(), threadId, null);
          }).execute();
        });
      });

      callButtonView.setOnClickListener(v -> {
        ContactUtil.selectRecipient(this, pushUsers, recipient -> CommunicationActions.startVoiceCall(this, recipient));
      });
    } else if (!systemUsers.isEmpty()) {
      inviteButtonView.setVisibility(View.VISIBLE);
      engageContainerView.setVisibility(View.GONE);

      inviteButtonView.setOnClickListener(v -> {
        ContactUtil.selectRecipient(this, pushUsers, recipient -> {
          new RetrieveThreadIdTask(this, recipient, threadId -> {
            CommunicationActions.startConversation(this, recipient.getAddress(), threadId, getString(R.string.InviteActivity_lets_switch_to_signal, "https://sgnl.link/1KpeYmF"));
          }).execute();
        });
      });
    } else {
      inviteButtonView.setVisibility(View.GONE);
      engageContainerView.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CODE_ADD_EDIT_CONTACT && contact != null) {
      new RefreshContactTask(this, contact).execute();
    }
  }
}
