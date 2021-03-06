package de.saschahlusiak.freebloks.game;

import java.io.IOException;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.controller.SpielClient;


class SpielClientThread extends Thread {
	private static final String tag = SpielClientThread.class.getSimpleName();

	private final SpielClient client;
	private boolean godown;

	SpielClientThread(SpielClient spiel) {
		super("SpielClientThread");
		this.client = spiel;
	}

	public SpielClient getClient()
	{
		return client;
	}

	private synchronized boolean getGoDown() {
		return godown;
	}

	public synchronized void goDown() {
		godown = true;
		client.disconnect();
	}

	private Exception error = null;

	public Exception getError() {
		return error;
	}

	@Override
	public void run() {
		godown = false;
		try {
			do {
				client.poll();
				if (getGoDown()) {
					return;
				}
			} while (client.isConnected());
		}
		catch (IOException e) {
			if (getGoDown())
				return;

			Crashlytics.logException(e);
			e.printStackTrace();

			synchronized(client) {
				error = e;
			}
		}
		finally {
			client.disconnect();
			Log.i(tag, "disconnected, thread going down");
		}
	}
}
