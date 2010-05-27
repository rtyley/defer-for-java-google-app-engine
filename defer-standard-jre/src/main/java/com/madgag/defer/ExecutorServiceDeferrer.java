package com.madgag.defer;

import java.util.concurrent.Executor;

public class ExecutorServiceDeferrer implements Deferrer {

	Executor executor;
	
	@Override
	public Object defer(Deferrable task) {
		return defer(task,"default");
	}

	@Override
	public Object defer(Deferrable task, String queueName) {
		executor.execute(task);
		return null;
	}

}
