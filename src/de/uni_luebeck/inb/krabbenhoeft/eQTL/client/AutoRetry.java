package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.CalculationInProgressException;

public abstract class AutoRetry<T> {
	public abstract void invoke(AsyncCallback<T> callback);

	public abstract void success(T result);

	public void run() {
		run(5000);
	}

	public void run(int timeoutProposal) {
		final int timeout = Math.max(500, timeoutProposal);
		new Timer() {
			public void run() {
				final Timer timer = this;
				invoke(new AsyncCallback<T>() {
					public void onFailure(Throwable caught) {

						final int displayDuration = timeout - 500;
						if (displayDuration > 333) {
							final MyMessages myMessages = (MyMessages) GWT.create(MyMessages.class);

							final String message;
							if (caught instanceof CalculationInProgressException)
								message = myMessages.calculationInProgress();
							else
								message = myMessages.autoRetryFailure(caught.getLocalizedMessage());

							ClientMainWindow.notifyUser(message, displayDuration);
						}
						timer.schedule(timeout);
					}

					public void onSuccess(T result) {
						success(result);
					}
				});
			}
		}.run();
	}
}
