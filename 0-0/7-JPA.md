### JPA

#### 1.常用注解
@Entity 实体
@Table 表
@Id
@GeneratedValue 生成策略
|- strategy：指定策略，AUTO/IDENTITY/SEQUENCE/TABLE 通过表产生键
示例：@TableGenerator(name = "pk_gen",
    table="tb_generator",
    pkColumnName="gen_name",
    valueColumnName="gen_value",
    pkColumnValue="PAYABLEMOENY_PK",
    allocationSize=1
)
@Column
@Transient
@Temporal 时间精度
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


