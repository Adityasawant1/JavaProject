<?php
$s=$_POST['s'];
if($s=="")
{
    echo "Enter the String";
}
elseif(strlen($s)<8)
{
    echo "Username should be atleast 8 characters";
}
else
{
        echo "$s";
}
?>