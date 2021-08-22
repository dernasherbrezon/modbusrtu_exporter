package ru.r2cloud.modbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;

import io.prometheus.client.Collector;

public class ModbusMasterCollector extends Collector {

	private static final Logger LOG = LoggerFactory.getLogger(ModbusMasterCollector.class);
	private static final Map<String, ModbusCollector> COLLECTORS = new HashMap<>();

	static {
		COLLECTORS.put("epeverTracer", new EpeverTracer());
	}

	private final ModbusSerialMaster master;
	private final ModbusCollector collector;

	public ModbusMasterCollector(Properties props) throws Exception {
		String collectorName = props.getProperty("server.collector");
		this.collector = COLLECTORS.get(collectorName);
		if (this.collector == null) {
			throw new IllegalArgumentException("unknown collector: " + collectorName);
		}
		SerialParameters parameters = new SerialParameters(props, "j2mod.");
		master = new ModbusSerialMaster(parameters, Integer.parseInt(props.getProperty("j2mod.timeout")));
		master.connect();
	}

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> result = new ArrayList<>();
		try {
			result.addAll(collector.collect(master));
		} catch (ModbusException e) {
			LOG.error("unable to read registers", e);
		}
		return result;
	}

}
