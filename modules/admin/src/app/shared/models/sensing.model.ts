/**
 * #uncommented
 * @class
 */
export class SensingModel {
  timestamp: string;
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
  error: boolean;
}
