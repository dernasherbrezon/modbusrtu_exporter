package ru.r2cloud.modbus;

import java.util.List;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;

import io.prometheus.client.Collector.MetricFamilySamples;

public interface ModbusCollector {

	List<MetricFamilySamples> collect(ModbusSerialMaster master) throws ModbusException;

}
