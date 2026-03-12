package xyz.lzw.nestful.service;

/**
 * @author Lzw
 * @date 2019/5/24
 * @since JDK 1.8
 */
public class TestEntity {
    public String name;
    public int age;

    public TestEntity() {
    }

    public TestEntity(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
