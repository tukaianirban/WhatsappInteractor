package com.example.whatsappinteractor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

public class ResolveWhatsappNumber extends AsyncTask<String, Void, Void>
{
    private Context context;
    private OnTaskCompleted listener;

    private String userWhatsappPhoneId;
    private Uri wappProfileUri, wappVoipcallUri, wappVideocallUri;

    private static final String LOGTAG = ResolveWhatsappNumber.class.getSimpleName();

    public ResolveWhatsappNumber(Context context, OnTaskCompleted listener)
    {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void v)
    {
        if (userWhatsappPhoneId == null || userWhatsappPhoneId.equals(""))
        {
            Log.d(LOGTAG,"unable to fetch whatsapp phoneId of contact");
            return;
        }

        if (wappProfileUri == null || wappVoipcallUri == null || wappVideocallUri == null)
        {
            Log.d(LOGTAG, "unable to fetch Uri for profile / voipcall / videocall");
            return;
        }

        listener.onTaskCompleted(wappProfileUri, wappVoipcallUri, wappVideocallUri, userWhatsappPhoneId);
    }

    @Override
    protected Void doInBackground(String... strings)
    {
        if (strings.length==0) return null;

        String fullName = strings[0];
        int contactId = -1;

        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null);
        if (cursor == null) return null;
        if (cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        }

        while (cursor.moveToNext())
        {
            String primaryDisplayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (fullName.equalsIgnoreCase(primaryDisplayName))
            {
                contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            }
        }
        cursor.close();

        if (contactId == -1) return null;

        Log.d("####", "contact id = " + contactId);

        //
        // search for the data row entry now
        //

        Cursor dataCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + " = ?", new String[] {String.valueOf(contactId)}, null);
        if (dataCursor == null) return null;
        if (dataCursor.getCount()==0)
        {

            dataCursor.close();
            return null;
        }

        while (dataCursor.moveToNext())
        {
            String accountType = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.ACCOUNT_TYPE_AND_DATA_SET));
            String mimeType = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

            // use only the profile mimetype of whatsapp data entry
            if (accountType.equals("com.whatsapp"))
            {
                if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.profile"))
                {
                    int rowid = dataCursor.getInt(dataCursor.getColumnIndex(ContactsContract.Data._ID));

                    wappProfileUri = ContactsContract.Data.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(rowid))
                            .build();
                    Log.d("####", "User profile uri=" + wappProfileUri);

                    String data1 = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DATA1));
                    if (data1 != null)
                    {
                        Log.d("###", "found user profile in whatsapp with data1=" + data1);
                        userWhatsappPhoneId = data1;
                    }
                }

                if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.voip.call"))
                {
                    int rowid = dataCursor.getInt(dataCursor.getColumnIndex(ContactsContract.Data._ID));

                    wappVoipcallUri = ContactsContract.Data.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(rowid))
                            .build();
                    Log.d("####", "User voipcall uri=" + wappVoipcallUri);
                }

                if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.video.call"))
                {
                    int rowid = dataCursor.getInt(dataCursor.getColumnIndex(ContactsContract.Data._ID));

                    wappVideocallUri = ContactsContract.Data.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(rowid))
                            .build();
                    Log.d("####", "User videocall uri=" + wappVideocallUri);
                }
            }
        }

        dataCursor.close();

        return null;
    }
}
