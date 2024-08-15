import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            // Carregar dados do arquivo
            List<Integer> dados = carregarDadosArquivo("dados500_mil.txt");

            // Criar instâncias das árvores
            AVLTree avlTree = new AVLTree();
            RedBlackTree rbTree = new RedBlackTree();

            // Medir tempo de preenchimento
            long tempoAvlPreenchimento = medirTempoPreenchimento(avlTree, dados);
            long tempoRbPreenchimento = medirTempoPreenchimento(rbTree, dados);

            System.out.println("Tempo de preenchimento AVL: " + tempoAvlPreenchimento + " ms");
            System.out.println("Tempo de preenchimento Rubro-Negra: " + tempoRbPreenchimento + " ms");

            // Medir tempo de operações com sorteios
            long tempoAvlOperacoes = medirTempoOperacoes(avlTree, 50000);
            long tempoRbOperacoes = medirTempoOperacoes(rbTree, 50000);

            System.out.println("Tempo de operações AVL: " + tempoAvlOperacoes + " ms");
            System.out.println("Tempo de operações Rubro-Negra: " + tempoRbOperacoes + " ms");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Carregar números do arquivo
    public static List<Integer> carregarDadosArquivo(String arquivo) throws FileNotFoundException {
        List<Integer> dados = new ArrayList<>();
        Scanner scanner = new Scanner(new File(arquivo));
        while (scanner.hasNextInt()) {
            dados.add(scanner.nextInt());
        }
        scanner.close();
        return dados;
    }

    // Medir o tempo de preenchimento de uma árvore
    public static long medirTempoPreenchimento(Tree tree, List<Integer> dados) {
        long inicio = System.currentTimeMillis();
        for (int numero : dados) {
            tree.insert(numero);
        }
        long fim = System.currentTimeMillis();
        return fim - inicio;
    }

    // Medir o tempo de operações com sorteios
    public static long medirTempoOperacoes(Tree tree, int qtdSorteios) {
        Random random = new Random();
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < qtdSorteios; i++) {
            int numero = random.nextInt(19999) - 9999;
            if (numero % 3 == 0) {
                tree.insert(numero);
            } else if (numero % 5 == 0) {
                tree.remove(numero);
            } else {
                int count = tree.count(numero);
                System.out.println("Número " + numero + " aparece " + count + " vezes na árvore");
            }
        }
        long fim = System.currentTimeMillis();
        return fim - inicio;
    }
}

// Interface para as árvores
interface Tree {
    void insert(int value);
    void remove(int value);
    int count(int value);
}

// Implementação da Árvore AVL
class AVLTree implements Tree {
    private class Node {
        int key, height;
        Node left, right;

        Node(int d) {
            key = d;
            height = 1;
        }
    }

    private Node root;

    private int height(Node N) {
        if (N == null)
            return 0;
        return N.height;
    }

    private Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    private int getBalance(Node N) {
        if (N == null)
            return 0;
        return height(N.left) - height(N.right);
    }

    @Override
    public void insert(int key) {
        root = insertRec(root, key);
    }

    private Node insertRec(Node node, int key) {
        if (node == null)
            return new Node(key);

        if (key < node.key)
            node.left = insertRec(node.left, key);
        else if (key > node.key)
            node.right = insertRec(node.right, key);
        else
            return node;

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = getBalance(node);

        if (balance > 1 && key < node.left.key)
            return rightRotate(node);

        if (balance < -1 && key > node.right.key)
            return leftRotate(node);

        if (balance > 1 && key > node.left.key) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        if (balance < -1 && key < node.right.key) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    @Override
    public void remove(int key) {
        root = deleteNode(root, key);
    }

    private Node deleteNode(Node root, int key) {
        if (root == null)
            return root;

        if (key < root.key)
            root.left = deleteNode(root.left, key);
        else if (key > root.key)
            root.right = deleteNode(root.right, key);
        else {
            if (root.left == null || root.right == null) {
                Node temp = root.left != null ? root.left : root.right;

                if (temp == null) {
                    root = null;
                } else {
                    root = temp;
                }
            } else {
                Node temp = minValueNode(root.right);
                root.key = temp.key;
                root.right = deleteNode(root.right, temp.key);
            }
        }

        if (root == null)
            return root;

        root.height = 1 + Math.max(height(root.left), height(root.right));

        int balance = getBalance(root);

        if (balance > 1 && getBalance(root.left) >= 0)
            return rightRotate(root);

        if (balance > 1 && getBalance(root.left) < 0) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }

        if (balance < -1 && getBalance(root.right) <= 0)
            return leftRotate(root);

        if (balance < -1 && getBalance(root.right) > 0) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }

        return root;
    }

    private Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }

    @Override
    public int count(int value) {
        return countOccurrences(root, value);
    }

    private int countOccurrences(Node node, int value) {
        if (node == null)
            return 0;

        if (node.key == value)
            return 1 + countOccurrences(node.left, value) + countOccurrences(node.right, value);
        else if (value < node.key)
            return countOccurrences(node.left, value);
        else
            return countOccurrences(node.right, value);
    }
}

// Implementação da Árvore Rubro-Negra
class RedBlackTree implements Tree {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        int key;
        Node left, right, parent;
        boolean color;

        Node(int key) {
            this.key = key;
            this.color = RED;
        }
    }

    private Node root;

    @Override
    public void insert(int key) {
        Node node = new Node(key);
        root = bstInsert(root, node);
        fixViolation(node);
    }

    private Node bstInsert(Node root, Node pt) {
        if (root == null)
            return pt;

        if (pt.key < root.key) {
            root.left = bstInsert(root.left, pt);
            root.left.parent = root;
        } else if (pt.key > root.key) {
            root.right = bstInsert(root.right, pt);
            root.right.parent = root;
        }
        return root;
    }

    private void rotateLeft(Node pt) {
        Node pt_right = pt.right;

        pt.right = pt_right.left;

        if (pt.right != null)
            pt.right.parent = pt;

        pt_right.parent = pt.parent;

        if (pt.parent == null)
            root = pt_right;
        else if (pt == pt.parent.left)
            pt.parent.left = pt_right;
        else
            pt.parent.right = pt_right;

        pt_right.left = pt;
        pt.parent = pt_right;
    }

    private void rotateRight(Node pt) {
        Node pt_left = pt.left;

        pt.left = pt_left.right;

        if (pt.left != null)
            pt.left.parent = pt;

        pt_left.parent = pt.parent;

        if (pt.parent == null)
            root = pt_left;
        else if (pt == pt.parent.left)
            pt.parent.left = pt_left;
        else
            pt.parent.right = pt_left;

        pt_left.right = pt;
        pt.parent = pt_left;
    }

    private void fixViolation(Node pt) {
        Node parent_pt = null;
        Node grand_parent_pt = null;

        while (pt != root && pt.color != BLACK && pt.parent != null && pt.parent.color == RED) {
            parent_pt = pt.parent;
            grand_parent_pt = pt.parent.parent;

            if (parent_pt == grand_parent_pt.left) {
                Node uncle_pt = grand_parent_pt.right;

                if (uncle_pt != null && uncle_pt.color == RED) {
                    grand_parent_pt.color = RED;
                    parent_pt.color = BLACK;
                    uncle_pt.color = BLACK;
                    pt = grand_parent_pt;
                } else {
                    if (pt == parent_pt.right) {
                        rotateLeft(parent_pt);
                        pt = parent_pt;
                        parent_pt = pt.parent;
                    }
                    rotateRight(grand_parent_pt);
                    boolean t = parent_pt.color;
                    parent_pt.color = grand_parent_pt.color;
                    grand_parent_pt.color = t;
                    pt = parent_pt;
                }
            } else {
                Node uncle_pt = grand_parent_pt.left;

                if (uncle_pt != null && uncle_pt.color == RED) {
                    grand_parent_pt.color = RED;
                    parent_pt.color = BLACK;
                    uncle_pt.color = BLACK;
                    pt = grand_parent_pt;
                } else {
                    if (pt == parent_pt.left) {
                        rotateRight(parent_pt);
                        pt = parent_pt;
                        parent_pt = pt.parent;
                    }
                    rotateLeft(grand_parent_pt);
                    boolean t = parent_pt.color;
                    parent_pt.color = grand_parent_pt.color;
                    grand_parent_pt.color = t;
                    pt = parent_pt;
                }
            }
        }

        if (pt == root) {
            pt.color = BLACK;
        }
    }

    @Override
    public void remove(int key) {
        Node node = deleteNode(root, key);
        if (node != null) {
            fixRemoveViolation(node);
        }
    }

    private Node deleteNode(Node root, int key) {
        Node nodeToDelete = root;
        Node replacement = null;
        Node child = null;
        boolean colorOriginal = BLACK;

        while (nodeToDelete != null) {
            if (key < nodeToDelete.key) {
                nodeToDelete = nodeToDelete.left;
            } else if (key > nodeToDelete.key) {
                nodeToDelete = nodeToDelete.right;
            } else {
                break;
            }
        }

        if (nodeToDelete == null) {
            return null;
        }

        colorOriginal = nodeToDelete.color;

        if (nodeToDelete.left == null) {
            child = nodeToDelete.right;
            replaceNode(nodeToDelete, nodeToDelete.right);
        } else if (nodeToDelete.right == null) {
            child = nodeToDelete.left;
            replaceNode(nodeToDelete, nodeToDelete.left);
        } else {
            Node successor = minValueNode(nodeToDelete.right);
            colorOriginal = successor.color;
            child = successor.right;

            if (successor.parent == nodeToDelete) {
                if (child != null) {
                    child.parent = successor;
                }
            } else {
                replaceNode(successor, successor.right);
                successor.right = nodeToDelete.right;
                successor.right.parent = successor;
            }

            replaceNode(nodeToDelete, successor);
            successor.left = nodeToDelete.left;
            successor.left.parent = successor;
            successor.color = nodeToDelete.color;
        }

        if (colorOriginal == BLACK) {
            fixRemoveViolation(child);
        }

        return nodeToDelete;
    }

    private void replaceNode(Node node, Node replacement) {
        if (node.parent == null) {
            root = replacement;
        } else if (node == node.parent.left) {
            node.parent.left = replacement;
        } else {
            node.parent.right = replacement;
        }
        if (replacement != null) {
            replacement.parent = node.parent;
        }
    }

    private void fixRemoveViolation(Node node) {
        while (node != root && (node == null || node.color == BLACK)) {
            if (node == node.parent.left) {
                Node sibling = node.parent.right;
                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    node.parent.color = RED;
                    rotateLeft(node.parent);
                    sibling = node.parent.right;
                }

                if ((sibling.left == null || sibling.left.color == BLACK) &&
                        (sibling.right == null || sibling.right.color == BLACK)) {
                    sibling.color = RED;
                    node = node.parent;
                } else {
                    if (sibling.right == null || sibling.right.color == BLACK) {
                        if (sibling.left != null) {
                            sibling.left.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateRight(sibling);
                        sibling = node.parent.right;
                    }
                    sibling.color = node.parent.color;
                    node.parent.color = BLACK;
                    if (sibling.right != null) {
                        sibling.right.color = BLACK;
                    }
                    rotateLeft(node.parent);
                    node = root;
                }
            } else {
                Node sibling = node.parent.left;
                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    node.parent.color = RED;
                    rotateRight(node.parent);
                    sibling = node.parent.left;
                }

                if ((sibling.right == null || sibling.right.color == BLACK) &&
                        (sibling.left == null || sibling.left.color == BLACK)) {
                    sibling.color = RED;
                    node = node.parent;
                } else {
                    if (sibling.left == null || sibling.left.color == BLACK) {
                        if (sibling.right != null) {
                            sibling.right.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateLeft(sibling);
                        sibling = node.parent.left;
                    }
                    sibling.color = node.parent.color;
                    node.parent.color = BLACK;
                    if (sibling.left != null) {
                        sibling.left.color = BLACK;
                    }
                    rotateRight(node.parent);
                    node = root;
                }
            }
        }
        if (node != null) {
            node.color = BLACK;
        }
    }

    private Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }

    @Override
    public int count(int value) {
        return countOccurrences(root, value);
    }

    private int countOccurrences(Node node, int value) {
        if (node == null)
            return 0;

        if (node.key == value)
            return 1 + countOccurrences(node.left, value) + countOccurrences(node.right, value);
        else if (value < node.key)
            return countOccurrences(node.left, value);
        else
            return countOccurrences(node.right, value);
    }
}
