export interface Color {
  prettyName: string;
  hex: string;
  htmlName: string;
}

export class ColorSet {
  readonly list: Color[] = [
    {prettyName: "Maroon",       hex: "#800000",  htmlName: "maroon"},
    {prettyName: "Crimson",      hex: "#DC143C",  htmlName: "crimson"},
    {prettyName: "Red",          hex: "#FF0000",  htmlName: "red"},
    {prettyName: "Pink",         hex: "#FFC0CB",  htmlName: "pink"},
    {prettyName: "Salmon",       hex: "#FA8072",  htmlName: "salmon"},
    {prettyName: "Coral",        hex: "#FF7F50",  htmlName: "coral"},
    {prettyName: "Dark Orange",  hex: "#FF8C00",  htmlName: "darkorange"},
    {prettyName: "Gold",         hex: "#FFD700",  htmlName: "gold"},
    {prettyName: "Light Green",  hex: "#90EE90",  htmlName: "lightgreen"},
    {prettyName: "Green",        hex: "#008000",  htmlName: "green"},
    {prettyName: "Olive",        hex: "#808000",  htmlName: "olive"},
    {prettyName: "Teal",         hex: "#008080",  htmlName: "teal"},
    {prettyName: "Aquamarine",   hex: "#66CDAA",  htmlName: "mediumaquamarine"},
    {prettyName: "Turquoise",    hex: "#40E0D0",  htmlName: "turquoise"},
    {prettyName: "Sky",          hex: "#87CEEB",  htmlName: "skyblue"},
    {prettyName: "Blue",         hex: "#0000CD",  htmlName: "blue"},
    {prettyName: "Dark Blue",    hex: "#000080",  htmlName: "darkblue"},
    {prettyName: "Indigo",       hex: "#4B0082",  htmlName: "indigo"},
    {prettyName: "Purple",       hex: "#663399",  htmlName: "rebeccapurple"},
    {prettyName: "Slate Blue",   hex: "#6A5ACD",  htmlName: "slateblue"},
    {prettyName: "Plum",         hex: "#DDA0DD",  htmlName: "plum"},
    {prettyName: "Fuschia",      hex: "#FF00FF",  htmlName: "fuschia"},
    {prettyName: "Slate Gray",   hex: "#708090",  htmlName: "slategray"},
    {prettyName: "Silver",       hex: "#C0C0C0",  htmlName: "silver"},
    {prettyName: "Gray",         hex: "#808080",  htmlName: "gray"},
    {prettyName: "Black",        hex: "#000000",  htmlName: "black"},
    {prettyName: "None",         hex: "transparent",  htmlName: "white"}
  ];

  /**
   * Get the set of colors
   * @return the static list of colors
   */
  getList(): Color[] {
    return this.list;
  }
}
