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
session_start();
$_SESSION['current']=time();
$_SESSION['s1']=explode(",",$_POST['s1']);
$total=0;
for($i=0;$i<count($_SESSION['s1']);$i++){
    $total+=$_SESSION['s1'][$i];
}
echo" <center><h1>MarkList</h1></center>";
echo"<h3>Name : ".$_SESSION['n']."</h3><h3>Class : ".$_SESSION['c']."</h3><h3>Address : ".$_SESSION['a']."</h3>";
echo"<table>
        <tr>
            <th>Subject</th>
            <th>Marks</th> 
        </tr>";
        for($i=0;$i<count($_SESSION['s1']);$i++){
            echo"<tr>
                    <td>Subject ".($i+1)."</td>
                    <td>".$_SESSION['s1'][$i]."</td>
                </tr>";
        }        
        echo"<tr>
            <td>Total</td>
            <td>".$total."</td>
        </tr>
        <tr>
            <td>Percentage</td>
            <td>".($total/count($_SESSION['s1']))."</td>
        </tr>
    </table>";
session_unset();
session_destroy();
?>