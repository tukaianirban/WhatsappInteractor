package com.example.whatsappinteractor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  the format of a contact's qualified domain name in WhatsApp
 *  is : <491712345678@s.whatsapp.net>
 */
public class MainActivity extends AppCompatActivity implements OnTaskCompleted
{
    private static int PERMISSIONS_REQUEST_CODE = 10;

    Button btnSendText, btnSendVoipCall, btnSendVideoCall, resolveWhatsappContact;

    private EditText tvUserFullName, tvTextToSend;
    private TextView tvUserProfile;

    private Uri wappProfileUri, wappVoipcallUri, wappVideocallUri;
    private String userWhatsappPhoneId;

    private String[] permissionsList = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (verifyContactspermissions())
        {
            initViews();
        }else
        {
            requestPermissions(permissionsList, PERMISSIONS_REQUEST_CODE);
        }
    }

    private void initViews()
    {
        setContentView(R.layout.activity_main);

        btnSendText = findViewById(R.id.btnSendText);
        btnSendVoipCall = findViewById(R.id.btnSendVoipCall);
        btnSendVideoCall = findViewById(R.id.btnSendVideoCall);

        resolveWhatsappContact = findViewById(R.id.resolveWhatsappContact);

        tvUserProfile = findViewById(R.id.userProfileQDN);

        tvUserFullName = findViewById(R.id.userFullName);
        tvTextToSend = findViewById(R.id.textToSend);

        btnSendText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (wappProfileUri == null)
                {
                    Toast.makeText(MainActivity.this,
                            "Insert a contact name and resolve it. Then retry this", Toast.LENGTH_LONG).show();
                    return;
                }
                sendWappText();
            }
        });
        resolveWhatsappContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (tvUserFullName.getText()== null) return;

                String userName = tvUserFullName.getText().toString();
                if (userName.length()==0) return;

                ResolveWhatsappNumber r = new ResolveWhatsappNumber(MainActivity.this, MainActivity.this);
                r.execute(userName);
            }
        });

        btnSendVoipCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (wappVoipcallUri == null)
                {
                    Toast.makeText(MainActivity.this,
                            "Insert a contact name and resolve it. Then retry this", Toast.LENGTH_LONG).show();
                    return;
                }
                sendWappVoipCall();
            }
        });

        btnSendVideoCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (wappVideocallUri == null)
                {
                    Toast.makeText(MainActivity.this,
                            "Insert a contact name and resolve it. Then retry this", Toast.LENGTH_LONG).show();
                    return;
                }
                sendWappVideoCall();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != PERMISSIONS_REQUEST_CODE) return;

        if (verifyContactspermissions())
        {
            initViews();
        }
    }

    @Override
    public void onTaskCompleted(Uri profileUri, Uri voipcallUri, Uri videocallUri, String profileqdn)
    {
        wappProfileUri = profileUri;
        wappVoipcallUri = voipcallUri;
        wappVideocallUri = videocallUri;
        userWhatsappPhoneId = profileqdn;

        tvUserProfile.setText(userWhatsappPhoneId);
    }

    private boolean verifyContactspermissions()
    {
        for (int i=0;i<permissionsList.length;i++)
        {
            if (checkSelfPermission(permissionsList[i]) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * launch a whatsapp voip call using an intent targetting the specific mimetype data row for the
     * user's whatsapp rawcontact.
     */
    private void sendWappVoipCall()
    {
        if (wappVoipcallUri == null) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(wappVoipcallUri, "vnd.android.cursor.item/vnd.com.whatsapp.voip.call");

        startActivity(intent);

    }

    /**
     * launch a whatsapp video call using an intent targetting the specific mimetype data row for the
     * user's whatsapp rawcontact.
     */
    private void sendWappVideoCall()
    {
        if (wappVoipcallUri == null) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(wappVideocallUri, "vnd.android.cursor.item/vnd.com.whatsapp.video.call");

        startActivity(intent);

    }

    /**
     * if an intent is sent targetting the contact's profile data row (.profile mimetype), then the
     * conversation page is opened but the message's text cannot be transported over.
     * to be able to send a text message to the user's conversation page, use whatsapp's exposed API
     * To just open the contact's conversation page, the methods of voip/video call can be used with
     * the right mimetype
     */
    private void sendWappText()
    {
        String phoneId = tvUserProfile.getText().toString();
        String text = tvTextToSend.getText().toString();

        if (phoneId.equals("")) return;

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.putExtra("jid", phoneId);
        sendIntent.setPackage("com.whatsapp");

        if (sendIntent.resolveActivity(getPackageManager()) == null)
        {
            Toast.makeText(this, "whatsapp application not resolved", Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(sendIntent);
    }


    // provided a full name, retrieve the user's whatsapp actionId
    // actionId = (qualified phone number with domain naming)

}
