<?php   
$ln=$_POST['ln'];
try
{
    $con=new PDO("pgsql:host=localhost;dbname=ty12429","postgres","root");
    $sql="select * from department where location='$ln'";
    $result=$con->query($sql);
    if($result->rowCount()>0)
    {
        echo "<table border=1>";
        echo "<tr><th>Id</th><th>Department Name</th><th>Location</th></tr>";
        while($row=$result->fetch())
        {
            echo "<tr>";
            echo "<td>$row[0]</td>";
            echo "<td>$row[1]</td>";
            echo "<td>$row[2]</td>";
            echo "</tr>";
        }
        echo "</table>";
    }
    else
    {
        echo "<marquee behavior='alternate' style='color:red'><h2>No Record Found!!</h2></marquee>";
    }
}
catch(PDOException $e)
{
    echo $e->getMessage();  
}
?>
<!-- 
create database ty12429;
\c ty12429;
create table department(id int primary key, name varchar(30),location varchar(30)); 
insert into department values(1,'CS','BMT');
insert into department values(2,'HR','Pune');
insert into department values(3,'Sales','Pune');
insert into department values(4,'Accounts','Pune');
insert into department values(5,'Marketing','Pune');
insert into department values(6,'IT','Mumbai');
insert into department values(7,'HR','Mumbai');
insert into department values(8,'Sales','Mumbai');
insert into department values(9,'Accounts','Mumbai');
insert into department values(10,'Marketing','Mumbai');
insert into department values(11,'IT','Delhi');
insert into department values(12,'HR','Delhi');
insert into department values(13,'Sales','Delhi'); -->

