# Cassandra
## Spring
Automatically config Mapper and Accessor.

Add the following codes into your spring configs.
```XML
<bean id="cassandraMapperScannerConfigurer"
    class="com.newtranx.util.cassandra.spring.MapperScannerConfigurer"
    p:basePackage="com.newtranx.myproject" />

<bean id="cassandraAccessorScannerConfigurer"
    class="com.newtranx.util.cassandra.spring.AccessorScannerConfigurer"
    p:basePackage="com.newtranx.myproject" />
```
Annotate your entity class:
```Java
@Table(name = "t_my_table")
@CassandraMapper("myEntityMapper")
public class MyEntity {
    
}
```
Then inject mapper:
```Java
@Autowired
@Qualifier("myEntityMapper")
private Mapper<MyEntity> mapper;
```
Inject accessor:
```Java
@Autowired
private MyAccessor myAccessor;
```