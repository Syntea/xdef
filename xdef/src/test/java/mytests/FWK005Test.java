package mytests;

//import org.junit.Test;
import org.xdef.xml.KXmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FWK005Test {

	public void test() throws InterruptedException {
		final int threads = 10;
		ExecutorService service = Executors.newFixedThreadPool(threads);
		List<Task> tasks = new ArrayList<>();
		for (int i = 0; i < threads; i++) {
			Task task = new Task(XML);
			tasks.add(task);
			service.execute(task);
		}
		waitUntilDone(tasks);
	}

	void waitUntilDone(List<Task> tasks) throws InterruptedException {
		boolean _continue;
		do {
			Boolean flag = null;
			for (Task task : tasks) {
				if (flag == null) {
					flag = task.isDone();
				} else {
					flag &= task.isDone();
				}
			}
			_continue = !flag;
			Thread.sleep(10);
		} while (_continue);
	}

	private static final String XML = "<A><B text=\"AqjfKfFLZu\"/></A>";

	public static void main(String... args) throws Exception {new FWK005Test().test();}

	private static class Task implements Runnable {
		private final String _xml;
		private boolean done;

		public Task(String xml) {_xml = xml;}

		@Override
		public void run() {
			try {
				for (int i = 0; i < 10; i++) { //i < 1_000_000;
					KXmlUtils.parseXml(_xml);
				}
			} finally {
				done = true;
			}
		}

		boolean isDone() {return done;}
	}
}
