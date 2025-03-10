<?php
    $fn=$_POST['n'];

    $data=file_get_contents($fn);
    $data=json_decode($data,true);

    foreach($data as $k => $v)
    {
        foreach($v as $k1 => $v1)
        {
            echo $k1." = ". $v1."<br>";
        }
    }
?>