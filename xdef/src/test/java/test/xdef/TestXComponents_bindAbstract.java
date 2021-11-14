package test.xdef;

import java.sql.Timestamp;
import org.xdef.sys.SDatetime;

public abstract class TestXComponents_bindAbstract {

	protected String name;
	protected Timestamp birth;
	protected TestXComponents_bindEnum sex;

	public TestXComponents_bindAbstract() {}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public Timestamp getBirth() {return birth;}
	public SDatetime getSBirth() {return new SDatetime(birth);}
	public void setBirth(Timestamp date) {this.birth = date;}
	public void setSBirth(SDatetime sBirth) {
		birth = SDatetime.getTimestamp(sBirth);
	}
	public TestXComponents_bindEnum getSex() {return sex;}
	public String getSexString() {return sex.toString();}
	public void setSex(TestXComponents_bindEnum sex) {this.sex = sex;  }
	public void setSexString(String s) {
		this.sex = TestXComponents_bindEnum.valueOf(s);
	}
}