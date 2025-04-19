<style>
    table{
        border-collapse: collapse;
        border: 1px solid black;
        width:50%
    }
    th,td{
        border: 1px solid black;
        padding: 10px;
    }
</style>
<?php
$id=$_POST['c'];
$name=$_POST['n'];
$add=$_POST['a'];
$city=$_POST['ci'];
$contact=$_POST['co'];
$ch=$_POST['m'];

switch($ch){
    case 1:
        $data=file_get_contents("Assign2SetB1.json");
        $data=json_decode($data,true);
        echo"<table>";
            echo"<tr>";
                echo"<th>Customer Id</th>";
                echo"<th>Customer Name</th>";
                echo"<th>Customer Address</th>";
                echo"<th>Customer City</th>";
                echo"<th>Customer Contact</th>";
            echo"</tr>";
            foreach($data as $k => $v)
            {
                
                    echo"<tr>";
                        echo"<td>";
                            echo "".$v['c_id'];
                        echo"</td>";
                        echo"<td>";
                            echo $v['c_name'];
                        echo"</td>";
                        echo"<td>";
                            echo $v['c_address'];
                        echo"</td>";
                        echo"<td>";
                            echo $v['c_city'];
                        echo"</td>";
                        echo"<td>";
                            echo $v['c_contact'];
                        echo"</td>";
                    echo"</tr>";
            }
        echo"</table>";
        break;
    case 2:
        $data=file_get_contents("Assign2SetB1.json");
        $data=json_decode($data,true);
        $data[]=['c_id'=>$id,'c_name'=>$name,'c_address'=>$add,'c_city'=>$city,'c_contact'=>$contact];
        $data=json_encode($data,JSON_PRETTY_PRINT);
        file_put_contents("Assign2SetB1.json",$data);
        break;

    case 3:
        $data=file_get_contents("Assign2SetB1.json");
        $data=json_decode($data,true);
        foreach($data as $k => $v)
        {
            if($v['c_id']==$id)
            {
                $data[$k]['c_contact']=$contact;
            }
        }
        $data=json_encode($data,JSON_PRETTY_PRINT);
        file_put_contents("Assign2SetB1.json",$data);
        break;
}
?>