package test.xdef;

public class TestXComponents_Y08 {
	Long idFlow;
	public Long getIdFlow() {return idFlow;}
	protected void setIdFlow(Long idFlow) {
		this.idFlow = idFlow == null ? null : idFlow + 1;
	}
}