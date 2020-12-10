package me.ignaciosanchez.jmx;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;

@Configuration
public class InfinispanJmxConfiguration {

	private final ObjectProvider<MBeanExporter> mBeanExporter;

	InfinispanJmxConfiguration(ObjectProvider<MBeanExporter> mBeanExporter) {
		this.mBeanExporter = mBeanExporter;
	}

	@PostConstruct
	public void validateMBeans() {
		// Whatever logic that is required to figure out if we should do our thing or not
		this.mBeanExporter
				.ifUnique((exporter) -> exporter.addExcludedBean("remoteCacheManager"));
	}

}