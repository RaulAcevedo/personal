package com.jjkeller.kmbapi.controller.utility;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class RunnableIntentService extends IntentService {
    private static final String ACTION_RUNNABLE = "com.jjkeller.kmbapi.controller.EOBR.action.RUNNABLE";

    private static Runnable intentAction;

    public RunnableIntentService() {
        super("RunnableIntentService");
    }

    /**
     * Starts this service to perform a runnable action, but
     * only if one isn't running already.
     *
     * @see IntentService
     */
    public static void startRunnableIntent(Context context, Runnable runnable) {
        if(intentAction == null) {
            intentAction = runnable;

            Intent intent = new Intent(context, RunnableIntentService.class);
            intent.setAction(ACTION_RUNNABLE);
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RUNNABLE.equals(action)) {
                handleRunnableIntent();
            }
        }
    }

    private void handleRunnableIntent() {
        try {
            intentAction.run();
        } finally {
          intentAction = null;
        }
    }
}
