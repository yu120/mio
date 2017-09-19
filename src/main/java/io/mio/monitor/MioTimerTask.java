package io.mio.monitor;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器定时任务
 *
 * @author lry
 */
public abstract class MioTimerTask extends TimerTask {
	
	private static final Logger logger = LoggerFactory.getLogger(MioTimerTask.class);
	
	private static Timer timer = new Timer("Mio-Timer", true);

	public MioTimerTask() {
		timer.schedule(this, getDelay(), getPeriod());
		logger.info("Regist MioTimerTask---- " + this.getClass().getSimpleName());
	}

	/**
	 * 获取定时任务的延迟启动时间
	 *
	 * @return
	 */
	protected long getDelay() {
		return 0;
	}


	public static void cancelQuickTask() {
		timer.cancel();
	}
	
	/**
	 * 获取定时任务的执行频率
	 *
	 * @return
	 */
	protected abstract long getPeriod();
	
}
