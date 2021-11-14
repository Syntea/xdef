package test.xdef;

import java.sql.Timestamp;

public interface TestXComponents_bindInterface {
	public String getName();
	public void setName(String name);
	public Timestamp getBirth();
	public void setBirth(Timestamp date);
	public TestXComponents_bindEnum getSex();
	public void setSex(TestXComponents_bindEnum x);
}