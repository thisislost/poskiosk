<%-- 
    Document   : index
    Created on : 06.04.2013, 21:44:12
    Author     : Maxim
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JavaPOS Simulator</title>
        <style>
            .ui_label {
                width: 150px;
                display: inline-block;
            }
            .ui_input {
                width: 200px;
            }
            .ui_submit {
                width: 100px;
            }
            .ui_change {
                width: 305px;
            }
            .ui_block {
                border: solid lightgrey; 
                border-width: thin;
                margin: 10px 0px 10px 0px;
                width: 500px;
                -moz-user-select: -moz-none;
                -o-user-select: none;
                -khtml-user-select: none;
                -webkit-user-select: none;
                user-select: none;
            }
            .ui_content {
                padding: 10px;
            }
            .ui_header {
                font-weight: bold;
                padding:  5px 10px 5px 10px;
                background: lightgray;
            }
        </style>
        <script>
            function show(el) {
                el.style.display = 'block';
            }
            function hide(el) {
                el.style.display = 'none';
            }
            function toggle(el, el1) {
                if (el === el1)
                    show(el1);
                else
                    hide(el1);
            }
            function selectDevice(name) {
                var el = document.getElementById('dd_' + name);
                toggle(el, dd_billacceptor);
                toggle(el, dd_posprinter);
                toggle(el, dd_scanner);
            }
            function selectStation(name) {
                var el = document.getElementById('pp_' + name);
                toggle(el, pp_receipt);
                toggle(el, pp_journal);
                toggle(el, pp_slip);
            }
        </script>
    </head>
    <body>
        <div class="ui_block">
            <div class="ui_header">
                <label class="ui_label" for="dd_device">Device</label>
                <select class="ui_change" id="dd_device" onchange="selectDevice(this.value);">
                    <option selected value="billacceptor">Bill Acceptor</option>
                    <option value="posprinter">POS Printer</option>
                    <option value="scanner">Scanner</option>
                </select>    
            </div>
            <div class="ui_content" id="dd_billacceptor">
                <form action="http://localhost:4001/accept" target="output">
                    <label class="ui_label" for="ba_nomimal">Nominal</label>
                    <select class="ui_input" id="ba_nomimal" name="nominal">
                        <option value="10">10</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                        <option value="500">500</option>
                        <option value="1000">1000</option>
                        <option value="5000">5000</option>
                    </select>
                    <input class="ui_submit" type="submit" value="Accept"/>
                </form>
                <form action="http://localhost:4001/jam" target="output">
                    <label class="ui_label" for="ba_jam">Jam status</label>
                    <select class="ui_change" id="ba_jam" name="status" onchange="this.form.submit();">
                        <option value="jam">JAM</option>
                        <option selected value="jamok">JAMOK</option>
                    </select>    
                </form>
                <form action="http://localhost:4001/full" target="output">
                    <label class="ui_label" for="ba_full">Full status</label>
                    <select class="ui_change" id="ba_full" name="status" onchange="this.form.submit();">
                        <option value="full">FULL</option>
                        <option value="nearfull">NEARFULL</option>
                        <option selected value="fullok">FULLOK</option>
                    </select>    
                </form>
                <form action="http://localhost:4001/power" target="output">
                    <label class="ui_label" for="ba_power">Power state</label>
                    <select class="ui_change" id="ba_power" name="state" onchange="this.form.submit();">
                        <option value="off">OFF</option>
                        <option value="offline">OFFLINE</option>
                        <option value="off_offline">OFF_OFFLINE</option>
                        <option selected value="online">ONLINE</option>
                    </select>    
                </form>
            </div>
            <div class="ui_content" id="dd_posprinter" hidden>
                <form action="http://localhost:4002/output" target="output">
                    <label class="ui_label" for="pp_station">Station</label>
                    <select class="ui_input" id="pp_station" name="station" onchange="selectStation(this.value);">
                        <option value="journal">JOURNAL</option>
                        <option selected value="receipt">RECEIPT</option>
                        <option value="slip">SLIP</option>
                    </select>
                    <input class="ui_submit" type="submit" value="Output"/>
                </form>
                <div id="pp_journal" hidden>
                    <form action="http://localhost:4002/cartridge" target="output">
                        <input type="hidden" name="station" value="journal"/>
                        <label class="ui_label" for="pp_jrn_cartridge">Cartridge state</label>
                        <select class="ui_change" id="pp_jrn_cartridge" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="empty">EMPTY</option>
                            <option value="nearempty">NEAREMPTY</option>
                        </select>    
                    </form>
                    <form action="http://localhost:4002/paper" target="output">
                        <input type="hidden" name="station" value="journal"/>
                        <label class="ui_label" for="pp_jrn_paper">Paper state</label>
                        <select class="ui_change" id="pp_jrn_paper" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="empty">EMPTY</option>
                            <option value="nearempty">NEAREMPTY</option>
                        </select>    
                    </form>
                    <form action="http://localhost:4002/cover" target="output">
                        <input type="hidden" name="station" value="journal"/>
                        <label class="ui_label" for="pp_jrn_cover">Cover state</label>
                        <select class="ui_change" id="pp_jrn_cover" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="open">OPEN</option>
                        </select>    
                    </form>
                </div>
                <div id="pp_receipt">
                    <form action="http://localhost:4002/cartridge" target="output">
                        <input type="hidden" name="station" value="receipt"/>
                        <label class="ui_label" for="pp_rec_cartridge">Cartridge state</label>
                        <select class="ui_change" id="pp_rec_cartridge" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="empty">EMPTY</option>
                            <option value="nearempty">NEAREMPTY</option>
                        </select>    
                    </form>
                    <form action="http://localhost:4002/paper" target="output">
                        <input type="hidden" name="station" value="receipt"/>
                        <label class="ui_label" for="pp_rec_paper">Paper state</label>
                        <select class="ui_change" id="pp_rec_paper" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="empty">EMPTY</option>
                            <option value="nearempty">NEAREMPTY</option>
                        </select>    
                    </form>
                    <form action="http://localhost:4002/cover" target="output">
                        <input type="hidden" name="station" value="receipt"/>
                        <label class="ui_label" for="pp_rec_cover">Cover state</label>
                        <select class="ui_change" id="pp_rec_cover" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="open">OPEN</option>
                        </select>    
                    </form>
                </div>
                <div id="pp_slip" hidden>
                    <form action="http://localhost:4002/cartridge" target="output">
                        <input type="hidden" name="station" value="slip"/>
                        <label class="ui_label" for="pp_slp_cartridge">Cartridge state</label>
                        <select class="ui_change" id="pp_slp_cartridge" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="empty">EMPTY</option>
                            <option value="nearempty">NEAREMPTY</option>
                        </select>    
                    </form>
                    <form action="http://localhost:4002/paper" target="output">
                        <input type="hidden" name="station" value="slip"/>
                        <label class="ui_label" for="pp_slp_paper">Paper state</label>
                        <select class="ui_change" id="pp_slp_paper" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="empty">EMPTY</option>
                            <option value="nearempty">NEAREMPTY</option>
                        </select>    
                    </form>
                    <form action="http://localhost:4002/cover" target="output">
                        <input type="hidden" name="station" value="slip"/>
                        <label class="ui_label" for="pp_slp_cover">Cover state</label>
                        <select class="ui_change" id="pp_slp_cover" name="state" onchange="this.form.submit();">
                            <option selected value="ok">OK</option>
                            <option value="open">OPEN</option>
                        </select>    
                    </form>
                </div>
                <form action="http://localhost:4002/cover" target="output">
                    <label class="ui_label" for="pp_cover">Common cover state</label>
                    <select class="ui_change" id="pp_cover" name="state" onchange="this.form.submit();">
                        <option selected value="ok">OK</option>
                        <option value="open">OPEN</option>
                    </select>    
                </form>
                <form action="http://localhost:4002/power" target="output">
                    <label class="ui_label" for="pp_power">Power state</label>
                    <select class="ui_change" id="pp_power" name="state" onchange="this.form.submit();">
                        <option value="off">OFF</option>
                        <option value="offline">OFFLINE</option>
                        <option value="off_offline">OFF_OFFLINE</option>
                        <option selected value="online">ONLINE</option>
                    </select>    
                </form>
            </div>
            <div class="ui_content" id="dd_scanner" hidden>
                <form action="http://localhost:4003/input" target="output">
                    <label class="ui_label" for="sc_data">Scan data</label>
                    <input class="ui_change" id="sc_data" name="data"/>
                    <label class="ui_label" for="sc_type">Scan data type</label>
                    <select class="ui_input" id="sc_type" name="type">
                        <option value="Codabar">Codabar</option>
                        <option selected value="Code128">Code128</option>
                        <option value="Code39">Code39</option>
                        <option value="DATAMATRIX">DATAMATRIX</option>
                        <option value="EAN128">EAN128</option>
                        <option value="QRCODE">QRCODE</option>
                        <option value="Other">Other</option>
                    </select>
                    <input class="ui_submit" type="submit" value="Input"/>
                </form>
                <form action="http://localhost:4003/power" target="output">
                    <label class="ui_label" for="pp_power">Power state</label>
                    <select class="ui_change" id="pp_power" name="state" onchange="this.form.submit();">
                        <option value="off">OFF</option>
                        <option value="offline">OFFLINE</option>
                        <option value="off_offline">OFF_OFFLINE</option>
                        <option selected value="online">ONLINE</option>
                    </select>    
                </form>
            </div>
        </div>
        <div class="ui_block">
            <div class="ui_header">Output</div>
            <iframe seamless sandbox name="output"></iframe>
        </div>
    </body>
</html>
