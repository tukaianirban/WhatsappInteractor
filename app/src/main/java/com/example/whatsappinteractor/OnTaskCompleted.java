package com.example.whatsappinteractor;

import android.net.Uri;

public interface OnTaskCompleted
{
    void onTaskCompleted(Uri wappProfileUri,Uri wappVoipcallUri, Uri wappVideocallUri, String userWhatsappPhoneId);
}
