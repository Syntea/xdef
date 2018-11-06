package test.xdef;

public class TestXComponents_Y04 {
	private String _One;
	public String getJedna() {return _One;}
	public void setJedna(String One) {_One = One.equals("1") ? "One" : One;}
	public void run() {}
}