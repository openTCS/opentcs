package org.opentcs.customadapter;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 定義車輛配置的接口。
 */
public interface VehicleConfigurationInterface {
  /**
   * 獲取充電操作的名稱。
   *
   * @return 充電操作的字符串表示。
   */
  String getRechargeOperation();

  /**
   * 獲取命令容量。
   *
   * @return 命令隊列的最大容量。
   */
  int getCommandsCapacity();

  /**
   * 獲取執行器服務。
   *
   * @return 用於執行異步任務的 ScheduledExecutorService。
   */
  ScheduledExecutorService getExecutorService();
}
