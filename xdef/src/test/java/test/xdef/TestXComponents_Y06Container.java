package test.xdef;

import java.io.Serializable;

public class TestXComponents_Y06Container <T extends TestXComponents_Y06Domain>
implements TestXComponents_Y06DomainContainer<T>, Serializable {
	T domain;

	@Override
	public T getDomain() {return this.domain;}
	@Override
	public void setDomain(T domain) {this.domain = domain;}
}