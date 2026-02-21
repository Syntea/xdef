package test.xdef;

import java.io.Serializable;

public class TestXComponents_Y08 implements Serializable {
	private Integer idFlow;
	public Integer getIdFlow() {return idFlow;}
	protected void setIdFlow(Integer idFlow) {this.idFlow = idFlow == null ? null : idFlow + 1;}
}