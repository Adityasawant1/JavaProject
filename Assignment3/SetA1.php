<?php
if($_SERVER['REQUEST_METHOD']=="POST")
{
    $num1=$_POST['num1'];
    $num2=$_POST['num2'];
}
elseif($_SERVER['REQUEST_METHOD']=="GET")
{   
    $num1=$_GET['num1'];
    $num2=$_GET['num2'];
}
echo $num1+$num2;
?>