package test;

import java.util.UUID;

import org.springframework.util.AlternativeJdkIdGenerator;

public class SpringCoreTest {
	public static void main(String[] args) {
		AlternativeJdkIdGenerator altJdkIdGen=new AlternativeJdkIdGenerator();
		UUID gid=altJdkIdGen.generateId();
		System.out.println(gid);
	}
}
