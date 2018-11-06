package test.xdef;

public class TestXComponents_Y08 {
	private Integer idFlow;
	public Integer getIdFlow() {return idFlow;}
	protected void setIdFlow(Integer idFlow) {
		this.idFlow = idFlow == null ? null : idFlow + 1;
	}
}