<?php
$r=$_POST['r'];
$c=$_POST['p'];

$data=file_get_contents("stud.json");
$data=json_decode($data,true);

foreach($data as $k => $v)
{
    if($v['rollno']=="$r")
    {
        $data[$k]['contact']="$c";
        echo "Updated Successfully";
        break;

    }
}
file_put_contents("stud.json",json_encode($data,JSON_PRETTY_PRINT));
?>