package com.dexterind.grovepi.sensors.base;

import com.dexterind.grovepi.utils.Commands;

import java.io.IOException;

public class DigitalSensor extends Sensor {
  protected int pin = 0;

  public DigitalSensor(int pin) throws IOException, InterruptedException {
	super();
	this.pin = pin;
  }
  
  public byte[] readBytes() throws IOException {
	this.board.writeI2c(Commands.DREAD, this.pin, Commands.UNUSED, Commands.UNUSED);
	return this.board.readI2c(1);
  }
  
  public boolean write(int value) throws IOException {
	this.board.writeI2c(Commands.DWRITE, this.pin, value, Commands.UNUSED);
	return true;
  }
}