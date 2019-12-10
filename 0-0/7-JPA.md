### JPA

#### 1.常用注解
@Entity 实体
@Table 表
@Id
@GeneratedValue 生成策略
|- strategy：指定策略，AUTO/IDENTITY/SEQUENCE/TABLE 通过表产生键
示例：@TableGenerator(name = "pk_gen",
    table="tb_generator",//表名
    pkColumnName="gen_name",//主键表的列名称
    valueColumnName="gen_value", //主键表的列名称
    pkColumnValue="PAYABLEMOENY_PK", //当前表的主键列在主键表中的名称
    allocationSize=1
)
@Column
@Transient 忽略
修改时间默认值示例：
@UpdateTimestamp
@Temporal(TemporalType.TIMESTAMP)
private Date updateTime;
//创建时间默认值示例：
@Temporal(TemporalType.TIMESTAMP)
@Column(name = "t_time")
@CreationTimestamp
private Date time;

一对一
@OneToOne(cascade=CascadeType.ALL)
@JoinColumn(name = "address_id", referencedColumnName = "id")
一端(Author)使用@OneToMany注释的mappedBy="author"属性表明Author是关系被维护端
一对多
@OneToMany 和 @ManyToOne
@JoinTable(name = "people_address",
            joinColumns = @JoinColumn(name="people_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id"))
多对多
@ManyToMany
@JoinTable(name = "user_authority",joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "authority_id"))




