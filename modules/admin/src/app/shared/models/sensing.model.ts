/**
 * @class
 * This represents a snapshot of asensor.
 *
 * This more than likely will be drastically changed once sensor-querying is implemented
 */
export class SensingModel {

  /** record when these values were queried */
  timestamp: string;

  /**
   * the data that this sensor holds. Almost certainly this will be broken up into different sensor classes,
   * unless all virtues have the same sensors and all sensors have the same update rates, areas of relevance,
   * and use cases.
   */
  sensors: [
    {
      virtue_id: string,
      username: string,
      updated_at: string,
      sensor_id: string,
      public_key: string,
      port: number,
      last_sync_at: string,
      kafka_topic: string,
      inserted_at: string,
      has_registered: boolean,
      has_certificates: boolean,
      configuration_id: number,
      component_id: number,
      address: string
    }
  ];

  /** if the data isn't functioning, for any reason. */
  error: boolean;
}
