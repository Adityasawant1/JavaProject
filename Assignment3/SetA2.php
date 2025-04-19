<?php
$s=$_POST['s'];
$a=array('Programming in C','Programming in C++','Programming in Java','Programming in Python','Programming in PHP');
$result="";
foreach($a as $temp)
{
    if(preg_match("/^$s/",$temp))
    {
        $result.="<br>".$temp;
    }
}
if($result=="")
{
    echo "No Match Found";
}
else
{
    echo $result;
}
?>