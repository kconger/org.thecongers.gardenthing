package com.dexterind.grovepi.sensors.base;

import java.io.IOException;

public class I2cSensor extends Sensor {
  protected int pin = 0;

  public I2cSensor(int pin) throws IOException, InterruptedException {
	super();
	this.pin = pin;
  }
}