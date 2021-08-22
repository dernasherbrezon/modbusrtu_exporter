package ru.r2cloud.modbus;

import java.util.ArrayList;
import java.util.List;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.GaugeMetricFamily;

public class EpeverTracer implements ModbusCollector {

	@Override
	public List<MetricFamilySamples> collect(ModbusSerialMaster master) throws ModbusException {
		List<MetricFamilySamples> result = new ArrayList<>();
		result.addAll(readElectricalParameters(0x3100, "solar", master));
		result.addAll(readElectricalParameters(0x3104, "battery", master));
		result.addAll(readElectricalParameters(0x310C, "load", master));

		InputRegister[] registers = master.readInputRegisters(0x3110, 3);
		result.add(new GaugeMetricFamily("batteryTemperature", "battery Temperature in degree Celsius", registers[0].getValue() / 100.0));
		result.add(new GaugeMetricFamily("deviceTemperature", "temperature inside case in degree Celsius", registers[1].getValue() / 100.0));
		result.add(new GaugeMetricFamily("componentsTemperature", "Heat sink surface temperature of equipments' power components in degree Celsius", registers[2].getValue() / 100.0));

		registers = master.readInputRegisters(0x311A, 2);
		result.add(new GaugeMetricFamily("batterySoc", "The percentage of battery's remaining capacity", registers[0].getValue() / 100.0));
		result.add(new GaugeMetricFamily("batteryRemoteTemperature", "The battery tempeture measured by remote temperature sensor in degree Celsius", registers[1].getValue() / 100.0));
		
		registers = master.readInputRegisters(0x311D, 1);
		result.add(new GaugeMetricFamily("batteryRealRatedPower", "Current system rated votlage in volts", registers[0].getValue() / 100.0));

		registers = master.readInputRegisters(0x3200, 2);
		result.add(new GaugeMetricFamily("batteryStatus",
				"D3-D0: 01H Overvolt , 00H Normal , 02H Under Volt, 03H Low Volt Disconnect, 04H Fault D7-D4: 00H Normal, 01H Over Temp.(Higher than the warning settings), 02H Low Temp.( Lower than the warning settings), D8: Battery inerternal resistance abnormal 1, normal 0 D15: 1-Wrong identification for rated voltage",
				registers[0].getValue()));
		result.add(new GaugeMetricFamily("equipmentStatus",
				"D15-D14: Input volt status. 00 normal, 01 no power connected, 02H Higher volt input, 03H Input volt error. D13: Charging MOSFET is short. D12: Charging or Anti-reverse MOSFET is short. D11: Anti-reverse MOSFET is short. D10: Input is over current. D9: The load is Over current. D8: The load is short. D7: Load MOSFET is short. D4: PV Input is short. D3-2: Charging status. 00 No charging,01 Float,02 Boost,03 Equlization. D1: 0 Normal, 1 Fault. D0: 1 Running, 0 Standby.",
				registers[1].getValue()));

		result.addAll(readElectricalParameters(0x3000, "ratedSolar", master));
		result.addAll(readElectricalParameters(0x3004, "ratedBattery", master));

		registers = master.readInputRegisters(0x300E, 1);
		result.add(new GaugeMetricFamily("ratedLoadCurrent", "Rated output current of load in ampers", registers[0].getValue() / 100.0));
		return result;
	}

	private static List<MetricFamilySamples> readElectricalParameters(int addressOffset, String paramPrefix, ModbusSerialMaster master) throws ModbusException {
		List<MetricFamilySamples> result = new ArrayList<>();
		InputRegister[] registers = master.readInputRegisters(addressOffset, 4);
		result.add(new GaugeMetricFamily(paramPrefix + "Voltage", "voltage in volts", registers[0].getValue() / 100.0));
		result.add(new GaugeMetricFamily(paramPrefix + "Current", "current in ampers", registers[1].getValue() / 100.0));
		int powerRaw = (registers[3].getValue() << 16) | registers[2].getValue();
		result.add(new GaugeMetricFamily(paramPrefix + "Power", "power in watts", powerRaw / 100.0));
		return result;
	}

}
